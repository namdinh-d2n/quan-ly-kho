
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Controllers.Statistics;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.Models;
using Xunit;

namespace SupermarketAPI.Tests.Epic4.US404;

public class OldestStockTests
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

    // Tạo danh mục
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

    // Tạo sản phẩm
    private static Product CreateProduct(
        Category category,
        int productId,
        string sku,
        string productName,
        int stockQuantity,
        bool status = true)
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
            StockQuantity = stockQuantity,
            MinStockLevel = 5,
            ImageUrl = null,
            CreatedAt = now,
            UpdatedAt = now,
            Status = status
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

    // Chuyển kết quả API thành JSON
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
    // TEST 1: LẤY SẢN PHẨM TỒN KHO LÂU NHẤT TRONG THÁNG NÀY
    // =========================================================

    [Fact]
    public async Task GetOldestStockProducts_ThisMonth_ShouldReturnCorrectProducts()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var oldestProduct = CreateProduct(
            category, 1, "SKU-OLD-001",
            "Sản phẩm nhập cũ nhất", 15
        );

        var newerProduct = CreateProduct(
            category, 2, "SKU-NEW-001",
            "Sản phẩm nhập mới hơn", 25
        );

        var zeroStockProduct = CreateProduct(
            category, 3, "SKU-ZERO-001",
            "Sản phẩm hết hàng", 0
        );

        var inactiveProduct = CreateProduct(
            category, 4, "SKU-INACTIVE-001",
            "Sản phẩm ngừng kinh doanh", 50, false
        );

        var outboundOnlyProduct = CreateProduct(
            category, 5, "SKU-OUT-001",
            "Sản phẩm chỉ có phiếu xuất", 10
        );

        var pendingProduct = CreateProduct(
            category, 6, "SKU-PENDING-001",
            "Sản phẩm có phiếu nhập chờ duyệt", 10
        );

        var rejectedProduct = CreateProduct(
            category, 7, "SKU-REJECTED-001",
            "Sản phẩm có phiếu nhập bị từ chối", 10
        );

        context.Products.AddRange(
            oldestProduct,
            newerProduct,
            zeroStockProduct,
            inactiveProduct,
            outboundOnlyProduct,
            pendingProduct,
            rejectedProduct
        );

        DateTime today = DateTime.Today;

        DateTime monthStart = new DateTime(
            today.Year, today.Month, 1
        );

        // Sản phẩm A: có hai lần nhập, phải lấy ngày nhập cũ nhất
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                101, "IMPORT-OLD-001", "INBOUND", "APPROVED",
                monthStart.AddHours(1)
            ),
            (oldestProduct, 30)
        );

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                102, "IMPORT-OLD-002", "INBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (oldestProduct, 20)
        );

        // Sản phẩm B: nhập mới hơn
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                103, "IMPORT-NEW-001", "INBOUND", "APPROVED",
                today.AddHours(10)
            ),
            (newerProduct, 40)
        );

        // Sản phẩm tồn kho bằng 0: không được lấy
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                104, "IMPORT-ZERO-001", "INBOUND", "APPROVED",
                monthStart.AddHours(2)
            ),
            (zeroStockProduct, 20)
        );

        // Sản phẩm ngừng kinh doanh: không được lấy
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                105, "IMPORT-INACTIVE-001", "INBOUND", "APPROVED",
                monthStart.AddHours(3)
            ),
            (inactiveProduct, 20)
        );

        // Chỉ có phiếu xuất: không được lấy
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                106, "OUTBOUND-ONLY-001", "OUTBOUND", "APPROVED",
                monthStart
            ),
            (outboundOnlyProduct, 20)
        );

        // Phiếu nhập đang chờ duyệt: không được lấy
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                107, "IMPORT-PENDING-001", "INBOUND", "PENDING",
                monthStart.AddHours(1)
            ),
            (pendingProduct, 20)
        );

        // Phiếu nhập bị từ chối: không được lấy
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                108, "IMPORT-REJECTED-001", "INBOUND", "REJECTED",
                monthStart.AddHours(1)
            ),
            (rejectedProduct, 20)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        // ACT: Lấy TOP 2 sản phẩm tồn kho lâu nhất
        var result = await controller.GetOldestStockProducts(
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

        // Sản phẩm A đứng đầu vì có ngày nhập cũ nhất
        Assert.Equal(
            1,
            data[0].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            "Sản phẩm nhập cũ nhất",
            data[0].GetProperty("productName").GetString()
        );

        Assert.Equal(
            15,
            data[0].GetProperty("currentStock").GetInt32()
        );

        DateTime oldestDate = data[0]
            .GetProperty("oldestImportDate")
            .GetDateTime();

        Assert.Equal(monthStart.AddHours(1), oldestDate);

        // Sản phẩm B đứng thứ hai
        Assert.Equal(
            2,
            data[1].GetProperty("productId").GetInt32()
        );
    }

    // =========================================================
    // TEST 2: KIỂM TRA BỘ LỌC 7 NGÀY
    // =========================================================

    [Fact]
    public async Task GetOldestStockProducts_SevenDays_ShouldRespectDateRange()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var inRangeProduct = CreateProduct(
            category, 10, "SKU-7D-IN",
            "Sản phẩm trong 7 ngày", 20
        );

        var outOfRangeProduct = CreateProduct(
            category, 11, "SKU-7D-OUT",
            "Sản phẩm ngoài 7 ngày", 30
        );

        context.Products.AddRange(
            inRangeProduct,
            outOfRangeProduct
        );

        DateTime today = DateTime.Today;

        // Phiếu nằm đúng ngày bắt đầu khoảng 7 ngày
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                201, "IMPORT-7D-001", "INBOUND", "APPROVED",
                today.AddDays(-6).AddHours(9)
            ),
            (inRangeProduct, 40)
        );

        // Phiếu ngoài 7 ngày
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                202, "IMPORT-7D-002", "INBOUND", "APPROVED",
                today.AddDays(-7).AddHours(9)
            ),
            (outOfRangeProduct, 50)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
            "7days", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            10,
            data[0].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            20,
            data[0].GetProperty("currentStock").GetInt32()
        );
    }

    // =========================================================
    // TEST 3: KIỂM TRA BỘ LỌC THÁNG TRƯỚC
    // =========================================================

    [Fact]
    public async Task GetOldestStockProducts_LastMonth_ShouldOnlyReturnLastMonthData()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var lastMonthProduct = CreateProduct(
            category, 20, "SKU-LAST-001",
            "Sản phẩm tháng trước", 20
        );

        var currentMonthProduct = CreateProduct(
            category, 21, "SKU-CURRENT-001",
            "Sản phẩm tháng này", 30
        );

        var oldProduct = CreateProduct(
            category, 22, "SKU-OLD-001",
            "Sản phẩm trước tháng trước", 40
        );

        context.Products.AddRange(
            lastMonthProduct,
            currentMonthProduct,
            oldProduct
        );

        DateTime today = DateTime.Today;

        DateTime thisMonthStart = new DateTime(
            today.Year, today.Month, 1
        );

        DateTime lastMonthStart = thisMonthStart.AddMonths(-1);

        // Phiếu tháng trước: được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                301, "LAST-IMPORT-001", "INBOUND", "APPROVED",
                lastMonthStart.AddDays(5).AddHours(9)
            ),
            (lastMonthProduct, 50)
        );

        // Phiếu tháng này: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                302, "THIS-IMPORT-001", "INBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (currentMonthProduct, 60)
        );

        // Phiếu trước tháng trước: không được tính
        AddReceiptWithDetails(
            context,
            CreateReceipt(
                303, "OLD-IMPORT-001", "INBOUND", "APPROVED",
                lastMonthStart.AddDays(-1)
            ),
            (oldProduct, 70)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
            "lastmonth", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            20,
            data[0].GetProperty("productId").GetInt32()
        );

        Assert.Equal(
            20,
            data[0].GetProperty("currentStock").GetInt32()
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
    public async Task GetOldestStockProducts_ShouldRespectTopLimit()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var productA = CreateProduct(
            category, 31, "SKU-TOP-001", "Sản phẩm A", 10
        );

        var productB = CreateProduct(
            category, 32, "SKU-TOP-002", "Sản phẩm B", 20
        );

        var productC = CreateProduct(
            category, 33, "SKU-TOP-003", "Sản phẩm C", 30
        );

        context.Products.AddRange(productA, productB, productC);

        DateTime today = DateTime.Today;

        DateTime monthStart = new DateTime(
            today.Year, today.Month, 1
        );

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                401, "TOP-IMPORT-001", "INBOUND", "APPROVED",
                monthStart.AddDays(3)
            ),
            (productA, 10)
        );

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                402, "TOP-IMPORT-002", "INBOUND", "APPROVED",
                monthStart.AddDays(2)
            ),
            (productB, 20)
        );

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                403, "TOP-IMPORT-003", "INBOUND", "APPROVED",
                monthStart.AddDays(1)
            ),
            (productC, 30)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        // Chỉ lấy hai sản phẩm có ngày nhập cũ nhất
        var result = await controller.GetOldestStockProducts(
            "thismonth", 2
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Equal(2, data.GetArrayLength());

        Assert.Equal(
            2,
            root.GetProperty("totalProducts").GetInt32()
        );

        // Sản phẩm C nhập sớm nhất
        Assert.Equal(
            33,
            data[0].GetProperty("productId").GetInt32()
        );

        // Sản phẩm B nhập sớm thứ hai
        Assert.Equal(
            32,
            data[1].GetProperty("productId").GetInt32()
        );
    }

    // =========================================================
    // TEST 5: FILTER KHÔNG HỢP LỆ
    // =========================================================

    [Theory]
    [InlineData("invalid")]
    [InlineData("")]
    [InlineData("quarter")]
    public async Task GetOldestStockProducts_InvalidFilter_ShouldReturnBadRequest(
        string filter)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
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
    public async Task GetOldestStockProducts_InvalidTop_ShouldReturnBadRequest(
        int top)
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
            "thismonth", top
        );

        Assert.IsType<BadRequestObjectResult>(result);
    }

    // =========================================================
    // TEST 7: KHÔNG CÓ DỮ LIỆU PHÙ HỢP
    // =========================================================

    [Fact]
    public async Task GetOldestStockProducts_NoMatchingData_ShouldReturnEmptyList()
    {
        using var context = CreateContext();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
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

    // =========================================================
    // TEST 8: CHUẨN HÓA FILTER
    // =========================================================

    [Fact]
    public async Task GetOldestStockProducts_ShouldAcceptUppercaseFilter()
    {
        using var context = CreateContext();

        var category = CreateCategory(context);

        var product = CreateProduct(
            category, 41, "SKU-CASE-001",
            "Sản phẩm kiểm thử", 25
        );

        context.Products.Add(product);

        DateTime today = DateTime.Today;

        AddReceiptWithDetails(
            context,
            CreateReceipt(
                501, "CASE-IMPORT-001", "INBOUND", "APPROVED",
                today.AddHours(9)
            ),
            (product, 30)
        );

        await context.SaveChangesAsync();

        var controller = new StatisticsController(context);

        var result = await controller.GetOldestStockProducts(
            " THISMONTH ", 5
        );

        using var document = ParseResult(result);

        var root = document.RootElement;
        var data = root.GetProperty("data");

        Assert.Single(data.EnumerateArray());

        Assert.Equal(
            41,
            data[0].GetProperty("productId").GetInt32()
        );

        // Response giữ nguyên filter đầu vào
        Assert.Equal(
            " THISMONTH ",
            root.GetProperty("filter").GetString()
        );
    }
}
