
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.DTOs.Statistics;

namespace SupermarketAPI.Controllers.Statistics;

[ApiController]
[Route("api/v1/statistics")]
[Authorize(Roles = "Admin")]
public class StatisticsController : ControllerBase
{
    private readonly MegaMartInventoryDbContext _context;

    public StatisticsController(MegaMartInventoryDbContext context)
    {
        _context = context;
    }

    // =========================================================
    // HÀM XÁC ĐỊNH KHOẢNG THỜI GIAN
    // =========================================================
    private static bool TryGetDateRange(
        string filter,
        out DateTime startDate,
        out DateTime endExclusive)
    {
        DateTime today = DateTime.Today;

        switch (filter.Trim().ToLowerInvariant())
        {
            case "7days":
                startDate = today.AddDays(-6);
                endExclusive = today.AddDays(1);
                return true;

            case "thismonth":
                startDate = new DateTime(today.Year, today.Month, 1);
                endExclusive = today.AddDays(1);
                return true;

            case "lastmonth":
                endExclusive = new DateTime(today.Year, today.Month, 1);
                startDate = endExclusive.AddMonths(-1);
                return true;

            default:
                startDate = default;
                endExclusive = default;
                return false;
        }
    }

    // =========================================================
    // HÀM XÁC ĐỊNH NGÀY ĐẦU TUẦN (THỨ 2)
    // =========================================================
    private static DateTime GetWeekStart(DateTime date)
    {
        int daysFromMonday = ((int)date.DayOfWeek + 6) % 7;
        return date.Date.AddDays(-daysFromMonday);
    }

    // =========================================================
    // API 1: BIỂU ĐỒ THỐNG KÊ NHẬP - XUẤT
    // GET /api/v1/statistics/chart
    // =========================================================
    [HttpGet("chart")]
    public async Task<IActionResult> GetChartStatistics(
        [FromQuery] string filter = "thismonth",
        [FromQuery] string groupBy = "day")
    {
        if (!TryGetDateRange(filter, out DateTime startDate,
                out DateTime endExclusive))
        {
            return BadRequest(new
            {
                success = false,
                message = "filter phải là 7days, thismonth hoặc lastmonth."
            });
        }

        groupBy = groupBy.Trim().ToLowerInvariant();

        if (groupBy != "day" && groupBy != "week")
        {
            return BadRequest(new
            {
                success = false,
                message = "groupBy phải là day hoặc week."
            });
        }

        // Lấy các phiếu nhập/xuất đã duyệt trong khoảng thời gian
        var receipts = await _context.InventoryReceipts
            .AsNoTracking()
            .Where(r =>
                r.Status == "APPROVED" &&
                r.ApprovedAt.HasValue &&
                r.ApprovedAt.Value >= startDate &&
                r.ApprovedAt.Value < endExclusive &&
                (r.ReceiptType == "INBOUND" ||
                 r.ReceiptType == "OUTBOUND"))
            .Select(r => new
            {
                ApprovedAt = r.ApprovedAt!.Value,
                r.ReceiptType,
                r.TotalQuantity
            })
            .ToListAsync();

        // Gom nhóm dữ liệu theo ngày hoặc tuần
        var totals = receipts
            .GroupBy(r => groupBy == "week"
                ? GetWeekStart(r.ApprovedAt)
                : r.ApprovedAt.Date)
            .ToDictionary(
                g => g.Key,
                g => (
                    TotalImport: g
                        .Where(x => x.ReceiptType == "INBOUND")
                        .Sum(x => x.TotalQuantity),

                    TotalExport: g
                        .Where(x => x.ReceiptType == "OUTBOUND")
                        .Sum(x => x.TotalQuantity)
                )
            );

        // Tạo danh sách đầy đủ các mốc thời gian
        var chartData = new List<ChartDataPointDto>();

        int stepDays = groupBy == "week" ? 7 : 1;

        DateTime firstBucket = groupBy == "week"
            ? GetWeekStart(startDate)
            : startDate;

        for (DateTime bucket = firstBucket;
             bucket < endExclusive;
             bucket = bucket.AddDays(stepDays))
        {
            DateTime bucketEnd = bucket.AddDays(stepDays);

            if (bucketEnd > endExclusive)
            {
                bucketEnd = endExclusive;
            }

            DateTime actualStart = bucket < startDate
                ? startDate
                : bucket;

            DateTime actualEnd = bucketEnd.AddDays(-1);

            totals.TryGetValue(bucket, out var total);

            string label = groupBy == "week"
                ? $"{actualStart:dd/MM} - {actualEnd:dd/MM}"
                : actualStart.ToString("dd/MM");

            chartData.Add(new ChartDataPointDto
            {
                Label = label,
                TotalImport = total.TotalImport,
                TotalExport = total.TotalExport
            });
        }

        return Ok(new
        {
            success = true,
            filter,
            groupBy,
            fromDate = startDate.ToString("yyyy-MM-dd"),
            toDate = endExclusive.AddDays(-1).ToString("yyyy-MM-dd"),
            data = chartData
        });
    }

    // =========================================================
    // API 2: TOP SẢN PHẨM XUẤT KHO NHIỀU NHẤT
    // GET /api/v1/statistics/top-exported
    // =========================================================
    [HttpGet("top-exported")]
    public async Task<IActionResult> GetTopExportedProducts(
        [FromQuery] string filter = "thismonth",
        [FromQuery] int top = 5)
    {
        if (!TryGetDateRange(filter, out DateTime startDate,
                out DateTime endExclusive))
        {
            return BadRequest(new
            {
                success = false,
                message = "filter phải là 7days, thismonth hoặc lastmonth."
            });
        }

        if (top < 1 || top > 50)
        {
            return BadRequest(new
            {
                success = false,
                message = "top phải nằm trong khoảng từ 1 đến 50."
            });
        }

        // JOIN ReceiptDetails, InventoryReceipts và Products
        var query =
            from detail in _context.ReceiptDetails.AsNoTracking()

            join receipt in _context.InventoryReceipts.AsNoTracking()
                on detail.ReceiptId equals receipt.Id

            join product in _context.Products.AsNoTracking()
                on detail.ProductId equals product.ProductId

            where receipt.ReceiptType == "OUTBOUND"
                  && receipt.Status == "APPROVED"
                  && receipt.ApprovedAt.HasValue
                  && receipt.ApprovedAt.Value >= startDate
                  && receipt.ApprovedAt.Value < endExclusive

            group detail by new
            {
                product.ProductId,
                product.Sku,
                product.ProductName,
                product.ImageUrl
            }
            into g

            select new TopExportedProductDto
            {
                ProductId = g.Key.ProductId,
                Sku = g.Key.Sku,
                ProductName = g.Key.ProductName,
                ImageUrl = g.Key.ImageUrl,
                TotalExportedQuantity = g.Sum(x => x.Quantity)
            };

        var result = await query
            .OrderByDescending(x => x.TotalExportedQuantity)
            .Take(top)
            .ToListAsync();

        return Ok(new
        {
            success = true,
            filter,
            fromDate = startDate.ToString("yyyy-MM-dd"),
            toDate = endExclusive.AddDays(-1).ToString("yyyy-MM-dd"),
            totalProducts = result.Count,
            data = result
        });
    }

    // =========================================================
    // API 3: TOP SẢN PHẨM TỒN KHO LÂU NHẤT
    // GET /api/v1/statistics/oldest-stock
    // =========================================================
    [HttpGet("oldest-stock")]
    public async Task<IActionResult> GetOldestStockProducts(
        [FromQuery] string filter = "thismonth",
        [FromQuery] int top = 5)
    {
        if (!TryGetDateRange(filter, out DateTime startDate,
                out DateTime endExclusive))
        {
            return BadRequest(new
            {
                success = false,
                message = "filter phải là 7days, thismonth hoặc lastmonth."
            });
        }

        if (top < 1 || top > 50)
        {
            return BadRequest(new
            {
                success = false,
                message = "top phải nằm trong khoảng từ 1 đến 50."
            });
        }

        // JOIN 3 bảng để tìm ngày nhập kho cũ nhất
        var query =
            from product in _context.Products.AsNoTracking()

            join detail in _context.ReceiptDetails.AsNoTracking()
                on product.ProductId equals detail.ProductId

            join receipt in _context.InventoryReceipts.AsNoTracking()
                on detail.ReceiptId equals receipt.Id

            where product.StockQuantity > 0
                  && product.Status == true
                  && receipt.ReceiptType == "INBOUND"
                  && receipt.Status == "APPROVED"
                  && receipt.ApprovedAt.HasValue
                  && receipt.ApprovedAt.Value >= startDate
                  && receipt.ApprovedAt.Value < endExclusive

            group receipt by new
            {
                product.ProductId,
                product.Sku,
                product.ProductName,
                product.ImageUrl,
                product.StockQuantity
            }
            into g

            select new OldestStockProductDto
            {
                ProductId = g.Key.ProductId,
                Sku = g.Key.Sku,
                ProductName = g.Key.ProductName,
                ImageUrl = g.Key.ImageUrl,
                CurrentStock = g.Key.StockQuantity,

                // Tìm ngày nhập kho cũ nhất
                OldestImportDate = g.Min(x => x.ApprovedAt)
            };

        var result = await query
            .OrderBy(x => x.OldestImportDate)
            .Take(top)
            .ToListAsync();

        return Ok(new
        {
            success = true,
            filter,
            fromDate = startDate.ToString("yyyy-MM-dd"),
            toDate = endExclusive.AddDays(-1).ToString("yyyy-MM-dd"),
            totalProducts = result.Count,
            data = result
        });
    }
}
