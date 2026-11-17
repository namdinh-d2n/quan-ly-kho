
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Controllers.Statistics;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.Models;
using Xunit;

namespace SupermarketAPI.Tests.Epic4.US404;

public class StatisticsChartTests
{
    // =========================================================
    // CÁC HÀM HỖ TRỢ
    // =========================================================

    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    // Tạo database InMemory riêng cho mỗi test
    private static MegaMartInventoryDbContext CreateContext()
    {
        var options = new DbContextOptionsBuilder<MegaMartInventoryDbContext>()
            .UseInMemoryDatabase(Guid.NewGuid().ToString())
            .Options;

        return new MegaMartInventoryDbContext(options);
    }

    // Tạo dữ liệu phiếu nhập hoặc xuất
    private static InventoryReceipt CreateReceipt(
        long id,
        string receiptCode,
        string receiptType,
        string status,
        DateTime? approvedAt,
        int totalQuantity)
    {
        DateTime now = DateTime.Today;

        return new InventoryReceipt
        {
            Id = id,
            ReceiptCode = receiptCode,
            ReceiptType = receiptType,
            Status = status,
            ApprovedAt = approvedAt,
            CreatedBy = 1,
            TotalItems = 1,
            TotalQuantity = totalQuantity,
            CreatedAt = now,
            UpdatedAt = now
        };
    }

    // Chuyển kết quả API thành JSON để kiểm tra
    private static JsonDocument ParseResult(IActionResult result)
    {
        var okResult = Assert.IsType<OkObjectResult>(result);

        string json = JsonSerializer.Serialize(
            okResult.Value,
            JsonOptions
        );

        return JsonDocument.Parse(json);
    }

    // Tính tổng dữ liệu trong một thuộc tính
    private static int SumProperty(
        JsonElement data,
        string propertyName)
    {
        int total = 0;

        foreach (var item in data.EnumerateArray())
        {
            total += item.GetProperty(propertyName).GetInt32();
        }

        return total;
    }

    // =========================================================
    // TEST 1: THỐNG KÊ 7 NGÀY GẦN NHẤT
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_SevenDays_ShouldReturnCorrectData()
    {
        using var context = CreateContext();

        DateTime today = DateTime.Today;

        context.InventoryReceipts.AddRange(
            // Phiếu nhập đã duyệt hôm nay
            CreateReceipt(
                1, "TEST-IN-001", "INBOUND", "APPROVED",
                today.AddHours(9), 120
            ),

            // Phiếu xuất đã duyệt hôm nay
            CreateReceipt(
                2, "TEST-OUT-001", "OUTBOUND", "APPROVED",
                today.AddHours(13), 35
            ),

            // Phiếu nhập đã duyệt cách đây 6 ngày
            CreateReceipt(
                3, "TEST-IN-002", "INBOUND", "APPROVED",
                today.AddDays(-6).AddHours(9), 20
            ),

            // Phiếu xuất cách đây 7 ngày: ngoài khoảng lọc
            CreateReceipt(
                4, "TEST-OUT-002", "OUTBOUND", "APPROVED",
                today.AddDays(-7).AddHours(9), 900
            ),

            // Phiếu đang chờ duyệt: không được tính
            CreateReceipt(
                5, "TEST-IN-003", "INBOUND", "PENDING",
                today.AddHours(14), 500
            ),

            // Phiếu bị từ chối: không được tính
            CreateReceipt(
                6, "TEST-OUT-003", "OUTBOUND", "REJECTED",
                today.AddHours(15), 700
            ),

            // Phiếu đã duyệt nhưng chưa có ngày duyệt
            CreateReceipt(
                7, "TEST-IN-004", "INBOUND", "APPROVED",
                null, 800
            )
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        // ACT: Gọi API
        var result = await controller.GetChartStatistics(
            "7days", "day"
        );

        // ASSERT: Kiểm tra kết quả
        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        // Phải có đủ 7 mốc ngày
        Assert.Equal(7, data.GetArrayLength());

        // Tổng nhập = 120 + 20 = 140
        Assert.Equal(140, SumProperty(data, "totalImport"));

        // Tổng xuất = 35
        Assert.Equal(35, SumProperty(data, "totalExport"));

        Assert.Equal(
            "7days",
            root.GetProperty("filter").GetString()
        );

        Assert.Equal(
            "day",
            root.GetProperty("groupBy").GetString()
        );

        Assert.Equal(
            today.AddDays(-6).ToString("yyyy-MM-dd"),
            root.GetProperty("fromDate").GetString()
        );

        Assert.Equal(
            today.ToString("yyyy-MM-dd"),
            root.GetProperty("toDate").GetString()
        );
    }

    // =========================================================
    // TEST 2: THỐNG KÊ THÁNG NÀY
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_ThisMonth_ShouldOnlyReturnThisMonthData()
    {
        using var context = CreateContext();

        DateTime today = DateTime.Today;

        DateTime thisMonthStart = new DateTime(
            today.Year, today.Month, 1
        );

        DateTime lastMonthStart = thisMonthStart.AddMonths(-1);

        context.InventoryReceipts.AddRange(
            // Phiếu nhập tháng này
            CreateReceipt(
                10, "THIS-IN-001", "INBOUND", "APPROVED",
                thisMonthStart.AddHours(9), 100
            ),

            // Phiếu xuất tháng này
            CreateReceipt(
                11, "THIS-OUT-001", "OUTBOUND", "APPROVED",
                today.AddHours(10), 40
            ),

            // Phiếu tháng trước: không được tính
            CreateReceipt(
                12, "LAST-IN-001", "INBOUND", "APPROVED",
                lastMonthStart.AddDays(5), 900
            )
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            "thismonth", "day"
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal(100, SumProperty(data, "totalImport"));
        Assert.Equal(40, SumProperty(data, "totalExport"));

        Assert.Equal(
            thisMonthStart.ToString("yyyy-MM-dd"),
            root.GetProperty("fromDate").GetString()
        );

        Assert.Equal(
            today.ToString("yyyy-MM-dd"),
            root.GetProperty("toDate").GetString()
        );
    }

    // =========================================================
    // TEST 3: THỐNG KÊ THÁNG TRƯỚC
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_LastMonth_ShouldOnlyReturnLastMonthData()
    {
        using var context = CreateContext();

        DateTime today = DateTime.Today;

        DateTime thisMonthStart = new DateTime(
            today.Year, today.Month, 1
        );

        DateTime lastMonthStart = thisMonthStart.AddMonths(-1);

        context.InventoryReceipts.AddRange(
            // Phiếu nhập tháng trước
            CreateReceipt(
                20, "LAST-IN-001", "INBOUND", "APPROVED",
                lastMonthStart.AddDays(5), 200
            ),

            // Phiếu xuất tháng trước
            CreateReceipt(
                21, "LAST-OUT-001", "OUTBOUND", "APPROVED",
                lastMonthStart.AddDays(6), 80
            ),

            // Phiếu tháng này: không được tính
            CreateReceipt(
                22, "THIS-IN-001", "INBOUND", "APPROVED",
                today.AddHours(9), 900
            ),

            // Phiếu trước tháng trước: không được tính
            CreateReceipt(
                23, "OLD-IN-001", "INBOUND", "APPROVED",
                lastMonthStart.AddDays(-1), 700
            )
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            "lastmonth", "day"
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal(200, SumProperty(data, "totalImport"));
        Assert.Equal(80, SumProperty(data, "totalExport"));

        Assert.Equal(
            lastMonthStart.ToString("yyyy-MM-dd"),
            root.GetProperty("fromDate").GetString()
        );

        Assert.Equal(
            thisMonthStart.AddDays(-1).ToString("yyyy-MM-dd"),
            root.GetProperty("toDate").GetString()
        );
    }

    // =========================================================
    // TEST 4: NHÓM DỮ LIỆU THEO NGÀY
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_GroupByDay_ShouldReturnDailyLabels()
    {
        using var context = CreateContext();

        DateTime today = DateTime.Today;

        context.InventoryReceipts.AddRange(
            CreateReceipt(
                30, "DAY-IN-001", "INBOUND", "APPROVED",
                today.AddHours(9), 50
            ),

            CreateReceipt(
                31, "DAY-IN-002", "INBOUND", "APPROVED",
                today.AddDays(-1).AddHours(9), 70
            )
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            "7days", "day"
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal("day", root.GetProperty("groupBy").GetString());

        Assert.Equal(7, data.GetArrayLength());

        // Kiểm tra định dạng nhãn dd/MM
        foreach (var item in data.EnumerateArray())
        {
            string? label = item.GetProperty("label").GetString();

            Assert.NotNull(label);
            Assert.Equal(5, label!.Length);
            Assert.Equal('/', label[2]);
        }
    }

    // =========================================================
    // TEST 5: NHÓM DỮ LIỆU THEO TUẦN
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_GroupByWeek_ShouldReturnWeeklyData()
    {
        using var context = CreateContext();

        DateTime today = DateTime.Today;

        context.InventoryReceipts.AddRange(
            CreateReceipt(
                40, "WEEK-IN-001", "INBOUND", "APPROVED",
                today.AddHours(9), 100
            ),

            CreateReceipt(
                41, "WEEK-OUT-001", "OUTBOUND", "APPROVED",
                today.AddHours(10), 30
            )
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            "thismonth", "week"
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal("week", root.GetProperty("groupBy").GetString());

        Assert.True(data.GetArrayLength() > 0);

        Assert.Equal(100, SumProperty(data, "totalImport"));
        Assert.Equal(30, SumProperty(data, "totalExport"));

        // Kiểm tra định dạng nhãn tuần
        foreach (var item in data.EnumerateArray())
        {
            string? label = item.GetProperty("label").GetString();

            Assert.NotNull(label);
            Assert.Contains(" - ", label);
        }
    }

    // =========================================================
    // TEST 6: FILTER KHÔNG HỢP LỆ
    // =========================================================

    [Theory]
    [InlineData("invalid")]
    [InlineData("")]
    [InlineData("quarter")]
    public async Task GetChartStatistics_InvalidFilter_ShouldReturnBadRequest(
        string filter)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            filter, "day"
        );

        Assert.IsType<BadRequestObjectResult>(result);
    }

    // =========================================================
    // TEST 7: GROUPBY KHÔNG HỢP LỆ
    // =========================================================

    [Theory]
    [InlineData("month")]
    [InlineData("year")]
    [InlineData("invalid")]
    public async Task GetChartStatistics_InvalidGroupBy_ShouldReturnBadRequest(
        string groupBy)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            "thismonth", groupBy
        );

        Assert.IsType<BadRequestObjectResult>(result);
    }

    // =========================================================
    // TEST 8: CHUẨN HÓA FILTER VÀ GROUPBY
    // =========================================================

    [Fact]
    public async Task GetChartStatistics_ShouldNormalizeFilterAndGroupBy()
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetChartStatistics(
            " THISMONTH ", " DAY "
        );

        using var document = ParseResult(result);

        var root = document.RootElement;

        // Filter trong response giữ nguyên giá trị đầu vào
        Assert.Equal(
            " THISMONTH ",
            root.GetProperty("filter").GetString()
        );

        // GroupBy được chuẩn hóa thành chữ thường
        Assert.Equal(
            "day",
            root.GetProperty("groupBy").GetString()
        );
    }
}
