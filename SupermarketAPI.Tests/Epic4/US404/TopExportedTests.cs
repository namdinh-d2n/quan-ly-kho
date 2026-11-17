
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Controllers.Statistics;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.Models;
using Xunit;

namespace SupermarketAPI.Tests.Epic4.US404;

public class TopExportedTests
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    // =========================================================
    // CÁC HÀM HỖ TRỢ
    // =========================================================

    // Tạo database InMemory riêng cho mỗi test
    private static MegaMartInventoryDbContext CreateContext()
    {
        var options = new DbContextOptionsBuilder<MegaMartInventoryDbContext>()
            .UseInMemoryDatabase(Guid.NewGuid().ToString())
            .Options;

        return new MegaMartInventoryDbContext(options);
    }

    // Tạo danh mục sản phẩm
    private static Category CreateCategory(
        MegaMartInventoryDbContext context)
    {
        var category = new Category
        {
            CategoryId = 1,
            CategoryName = "Danh mục kiểm thử"
        };

        context.Categories.Add(category);

        return category;
    }

    // Tạo sản phẩm kiểm thử
    private static Product CreateProduct(
        Category category,
        int productId,
        string sku,
        string productName)
    {
        DateTime now = DateTime.Today;

        return new Product
        {
            ProductId = productId,
            CategoryId = category.CategoryId,
            Category = category,
            Sku = sku,
            ProductName = productName,
            Barcode = null,
            Unit = "Cái",
            ImportPrice = 1000,
            SellingPrice = 1500,
            StockQuantity = 100,
            MinStockLevel = 5,
            ImageUrl = null,
            CreatedAt = now,
            UpdatedAt = now,
            Status = true
        };
    }

    // Tạo phiếu nhập hoặc xuất
    private static InventoryReceipt CreateReceipt(
        long id,
        string receiptCode,
        string receiptType,
        string status,
        DateTime? approvedAt)
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
            TotalItems = 0,
            TotalQuantity = 0,
            CreatedAt = now,
            UpdatedAt = now
        };
    }

    // Tạo phiếu kèm chi tiết sản phẩm
    private static void AddReceiptWithDetails(
        MegaMartInventoryDbContext context,
        InventoryReceipt receipt,
        params (Product Product, int Quantity)[] items)
    {
        context.InventoryReceipts.Add(receipt);

        for (int i = 0; i < items.Length; i++)
        {
            var item = items[i];

            context.ReceiptDetails.Add(new ReceiptDetail
            {
                Id = receipt.Id * 100 + i + 1,
                ReceiptId = receipt.Id,
                ProductId = item.Product.ProductId,
                Sku = item.Product.Sku,
                ProductName = item.Product.ProductName,
                Quantity = item.Quantity,
                Product = item.Product,
                Receipt = receipt
            });
        }
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

    // =========================================================
    // TEST 1: LẤY TOP SẢN PHẨM XUẤT KHO NHIỀU NHẤT
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_ThisMonth_ShouldReturnCorrectTopProducts()
    {
        using var context = CreateContext();

        // ARRANGE: Tạo danh mục và sản phẩm
        var category = CreateCategory(context);

        var productA = CreateProduct(
            category, 1, "SKU-TEST-001", "Sản phẩm A"
        );

        var productB = CreateProduct(
            category, 2, "SKU-TEST-002", "Sản phẩm B"
        );

        var productC = CreateProduct(
            category, 3, "SKU-TEST-003", "Sản phẩm C"
        );

        context.Products.AddRange(productA, productB, productC);

        DateTime today = DateTime.Today;

        DateTime thisMonthStart = new DateTime(
            today.Year, today.Month, 1
        );

        DateTime lastMonthStart = thisMonthStart.AddMonths(-1);

        // Phiếu xuất đã duyệt thứ nhất
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                101, "EXPORT-001", "OUTBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (productA, 100),
            (productB, 50)
        );

        // Phiếu xuất đã duyệt thứ hai
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                102, "EXPORT-002", "OUTBOUND", "APPROVED",
                today.AddHours(10)
            ),
            (productA, 80),
            (productC, 120)
        );

        // Phiếu nhập đã duyệt: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                103, "IMPORT-001", "INBOUND", "APPROVED",
                today.AddHours(11)
            ),
            (productA, 900)
        );

        // Phiếu xuất đang chờ duyệt: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                104, "EXPORT-003", "OUTBOUND", "PENDING",
                today.AddHours(12)
            ),
            (productA, 700)
        );

        // Phiếu xuất bị từ chối: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                105, "EXPORT-004", "OUTBOUND", "REJECTED",
                today.AddHours(13)
            ),
            (productB, 800)
        );

        // Phiếu xuất tháng trước: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                106, "EXPORT-005", "OUTBOUND", "APPROVED",
                lastMonthStart.AddDays(5)
            ),
            (productC, 1000)
        );

        // Phiếu đã duyệt nhưng chưa có ngày duyệt: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                107, "EXPORT-006", "OUTBOUND", "APPROVED",
                null
            ),
            (productB, 2000)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        // ACT: Lấy TOP 2 sản phẩm xuất nhiều nhất tháng này
        var result = await controller.GetTopExportedProducts(
            "thismonth", 2
        );

        // ASSERT
        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal(2, data.GetArrayLength());

        Assert.Equal(
            2,
            root.GetProperty("totalProducts").GetInt32()
        );

        // Sản phẩm A: 100 + 80 = 180
        Assert.Equal(
            1,
            data[0].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            "Sản phẩm A",
            data[0].GetProperty("productName").GetString()
        );

        Assert.Equal(
            180,
            data[0].GetProperty("totalExportedQuantity").GetInt32()
        );

        // Sản phẩm C: 120
        Assert.Equal(
            3,
            data[1].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            120,
            data[1].GetProperty("totalExportedQuantity").GetInt32()
        );

        Assert.Equal(
            "thismonth",
            root.GetProperty("filter").GetString()
        );
    }

    // =========================================================
    // TEST 2: KIỂM TRA BỘ LỌC 7 NGÀY
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_SevenDays_ShouldRespectDateRange()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var product = CreateProduct(
            category, 10, "SKU-7D-001", "Sản phẩm 7 ngày"
        );

        context.Products.Add(product);

        DateTime today = DateTime.Today;

        // Phiếu xuất hôm nay
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                201, "EXPORT-7D-001", "OUTBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (product, 10)
        );

        // Phiếu xuất đúng ngày bắt đầu khoảng 7 ngày
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                202, "EXPORT-7D-002", "OUTBOUND", "APPROVED",
                today.AddDays(-6).AddHours(9)
            ),
            (product, 20)
        );

        // Phiếu xuất ngoài khoảng 7 ngày
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                203, "EXPORT-7D-003", "OUTBOUND", "APPROVED",
                today.AddDays(-7).AddHours(9)
            ),
            (product, 900)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            "7days", 5
        );

        using var document = ParseResult(result);

        var data = document.RootElement.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        // Chỉ tính 10 + 20 = 30
        Assert.Equal(
            30,
            data[0].GetProperty("totalExportedQuantity").GetInt32()
        );
    }

    // =========================================================
    // TEST 3: KIỂM TRA BỘ LỌC THÁNG TRƯỚC
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_LastMonth_ShouldOnlyReturnLastMonthData()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var product = CreateProduct(
            category, 20, "SKU-LAST-001", "Sản phẩm tháng trước"
        );

        context.Products.Add(product);

        DateTime today = DateTime.Today;

        DateTime thisMonthStart = new DateTime(
            today.Year, today.Month, 1
        );

        DateTime lastMonthStart = thisMonthStart.AddMonths(-1);

        // Phiếu tháng trước: được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                301, "LAST-EXPORT-001", "OUTBOUND", "APPROVED",
                lastMonthStart.AddDays(5)
            ),
            (product, 35)
        );

        // Phiếu tháng này: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                302, "THIS-EXPORT-001", "OUTBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (product, 600)
        );

        // Phiếu trước tháng trước: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                303, "OLD-EXPORT-001", "OUTBOUND", "APPROVED",
                lastMonthStart.AddDays(-1)
            ),
            (product, 800)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            "lastmonth", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            35,
            data[0].GetProperty("totalExportedQuantity").GetInt32()
        );

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
    // TEST 4: KIỂM TRA GIỚI HẠN TOP
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_ShouldRespectTopLimit()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var productA = CreateProduct(
            category, 31, "SKU-TOP-001", "Sản phẩm A"
        );

        var productB = CreateProduct(
            category, 32, "SKU-TOP-002", "Sản phẩm B"
        );

        var productC = CreateProduct(
            category, 33, "SKU-TOP-003", "Sản phẩm C"
        );

        context.Products.AddRange(productA, productB, productC);

        DateTime today = DateTime.Today;

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                401, "TOP-EXPORT-001", "OUTBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (productA, 10),
            (productB, 20),
            (productC, 30)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        // Chỉ lấy 1 sản phẩm đứng đầu
        var result = await controller.GetTopExportedProducts(
            "thismonth", 1
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            1,
            root.GetProperty("totalProducts").GetInt32()
        );

        // Sản phẩm C có số lượng xuất lớn nhất
        Assert.Equal(
            33,
            data[0].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            30,
            data[0].GetProperty("totalExportedQuantity").GetInt32()
        );
    }

    // =========================================================
    // TEST 5: FILTER KHÔNG HỢP LỆ
    // =========================================================

    [Theory]
    [InlineData("invalid")]
    [InlineData("")]
    [InlineData("quarter")]
    public async Task GetTopExportedProducts_InvalidFilter_ShouldReturnBadRequest(
        string filter)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            filter, 5
        );

        Assert.IsType<BadRequestObjectResult>(result);
    }

    // =========================================================
    // TEST 6: TOP KHÔNG HỢP LỆ
    // =========================================================

    [Theory]
    [InlineData(0)]
    [InlineData(-1)]
    [InlineData(51)]
    [InlineData(100)]
    public async Task GetTopExportedProducts_InvalidTop_ShouldReturnBadRequest(
        int top)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            "thismonth", top
        );

        Assert.IsType<BadRequestObjectResult>(result);
    }

    // =========================================================
    // TEST 7: CHUẨN HÓA FILTER
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_ShouldAcceptUppercaseFilter()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var product = CreateProduct(
            category, 41, "SKU-CASE-001", "Sản phẩm kiểm thử"
        );

        context.Products.Add(product);

        DateTime today = DateTime.Today;

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                501, "CASE-EXPORT-001", "OUTBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (product, 25)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            " THISMONTH ", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            25,
            data[0].GetProperty("totalExportedQuantity").GetInt32()
        );

        // Response giữ nguyên filter được gửi vào
        Assert.Equal(
            " THISMONTH ",
            root.GetProperty("filter").GetString()
        );
    }

    // =========================================================
    // TEST 8: KHÔNG CÓ DỮ LIỆU PHÙ HỢP
    // =========================================================

    [Fact]
    public async Task GetTopExportedProducts_NoMatchingData_ShouldReturnEmptyList()
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetTopExportedProducts(
            "thismonth", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Empty(data.EnumerateArray());

        Assert.Equal(
            0,
            root.GetProperty("totalProducts").GetInt32()
        );
    }
}
