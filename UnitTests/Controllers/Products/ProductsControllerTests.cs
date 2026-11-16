using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Moq;
using SupermarketAPI.Controllers.Products;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.DTOs.Product;
using SupermarketAPI.Models;
using Xunit;
using UnitTests.Helpers;

namespace UnitTests.Controllers
{
    public class ProductsControllerTests : IDisposable
    {
        private readonly MegaMartInventoryDbContext _context;
        private readonly Mock<IWebHostEnvironment> _mockEnv;
        private readonly ProductsController _controller;
        private readonly string _tempWebRootPath;

        public ProductsControllerTests()
        {
            var options = new DbContextOptionsBuilder<MegaMartInventoryDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            _context = new MegaMartInventoryDbContext(options);

            _tempWebRootPath = Path.Combine(Path.GetTempPath(), "SupermarketTest_" + Guid.NewGuid());
            Directory.CreateDirectory(_tempWebRootPath);

            _mockEnv = new Mock<IWebHostEnvironment>();
            _mockEnv.Setup(e => e.WebRootPath).Returns(_tempWebRootPath);

            _controller = new ProductsController(_context, _mockEnv.Object);
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();

            if (Directory.Exists(_tempWebRootPath))
            {
                Directory.Delete(_tempWebRootPath, recursive: true);
            }
        }

        private static IFormFile CreateMockFormFile(string fileName, string content = "fake-image-bytes")
        {
            var bytes = Encoding.UTF8.GetBytes(content);
            var stream = new MemoryStream(bytes);
            var mockFile = new Mock<IFormFile>();

            mockFile.Setup(f => f.FileName).Returns(fileName);
            mockFile.Setup(f => f.Length).Returns(bytes.Length);
            mockFile.Setup(f => f.OpenReadStream()).Returns(stream);
            mockFile.Setup(f => f.CopyToAsync(It.IsAny<Stream>(), It.IsAny<CancellationToken>()))
                .Returns((Stream target, CancellationToken token) =>
                {
                    stream.Position = 0;
                    return stream.CopyToAsync(target, token);
                });

            return mockFile.Object;
        }

        [Fact]
        public async Task GetProducts_ReturnsOnlyActiveProducts_SortedByCreatedAtDesc()
        {
            // Arrange
            _context.Categories.Add(new Category { CategoryId = 1, CategoryName = "Đồ uống" });

            var oldProduct = new Product
            {
                ProductId = 1,
                CategoryId = 1,
                ProductName = "Nước suối cũ",
                Sku = "SKU-001",
                SellingPrice = 5000,
                StockQuantity = 50,
                MinStockLevel = 10,
                Status = true,
                CreatedAt = DateTime.UtcNow.AddDays(-5)
            };

            var newestProduct = new Product
            {
                ProductId = 2,
                CategoryId = 1,
                ProductName = "Trà xanh mới",
                Sku = "SKU-002",
                SellingPrice = 10000,
                StockQuantity = 30,
                MinStockLevel = 10,
                Status = true,
                CreatedAt = DateTime.UtcNow
            };

            var inactiveProduct = new Product
            {
                ProductId = 3,
                CategoryId = 1,
                ProductName = "Sản phẩm ngừng bán",
                Sku = "SKU-003",
                SellingPrice = 15000,
                Status = false,
                CreatedAt = DateTime.UtcNow.AddDays(1)
            };

            _context.Products.AddRange(oldProduct, newestProduct, inactiveProduct);
            await _context.SaveChangesAsync();

            // Act
            var result = await _controller.GetProducts(search: null);

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            var apiResponse = Assert.IsType<ApiResponse<object>>(okResult.Value);
            Assert.True(apiResponse.Success);
            Assert.Equal(200, apiResponse.Code);

            var dataType = apiResponse.Data!.GetType();
            var totalItems = (int)dataType.GetProperty("totalItems")!.GetValue(apiResponse.Data)!;
            var items = ((IEnumerable<ProductResponse>)dataType.GetProperty("items")!.GetValue(apiResponse.Data)!).ToList();

            Assert.Equal(2, totalItems);
            Assert.Equal(2, items.Count);
            Assert.Equal(2, items[0].Id);
            Assert.Equal("Trà xanh mới", items[0].Name);
            Assert.Equal("Đồ uống", items[0].CategoryName);
            Assert.Equal(1, items[1].Id);
        }

        [Fact]
        public async Task GetProducts_WithSearchKeyword_FiltersByNameSkuOrBarcode()
        {
            // Arrange
            _context.Categories.Add(new Category { CategoryId = 1, CategoryName = "Bánh kẹo" });
            _context.Products.AddRange(
                new Product { ProductId = 1, CategoryId = 1, ProductName = "Bánh Oreo", Sku = "OREO-01", Barcode = "8930001", Status = true, CreatedAt = DateTime.UtcNow },
                new Product { ProductId = 2, CategoryId = 1, ProductName = "Kẹo Chupa Chups", Sku = "KEO-02", Barcode = "8930002", Status = true, CreatedAt = DateTime.UtcNow }
            );
            await _context.SaveChangesAsync();

            // Act
            var result = await _controller.GetProducts(search: "  8930002  ");

            // Assert
            var okResult = Assert.IsType<OkObjectResult>(result);
            var apiResponse = Assert.IsType<ApiResponse<object>>(okResult.Value);
            var items = ((IEnumerable<ProductResponse>)apiResponse.Data!.GetType().GetProperty("items")!.GetValue(apiResponse.Data)!).ToList();

            Assert.Single(items);
            Assert.Equal("KEO-02", items[0].Sku);
        }

        [Fact]
        public async Task CreateProduct_ValidRequestWithImage_Returns201AndSavesProductAndFile()
        {
            // Arrange
            _context.Categories.Add(new Category { CategoryId = 1, CategoryName = "Sữa tươi" });
            await _context.SaveChangesAsync();

            var dto = new CreateRequest
            {
                ProductName = "Sữa tươi Vinamilk 1L",
                CategoryId = 1,
                Sku = "MILK-1L",
                Barcode = "8934673123456",
                Unit = "Hộp",
                ImportPrice = 25000,
                SellingPrice = 32000,
                StockQuantity = 100,
                MinStockLevel = 20,
                ImageFile = CreateMockFormFile("vinamilk.png")
            };

            // Act
            var result = await _controller.CreateProduct(dto);

            // Assert
            var statusResult = Assert.IsType<ObjectResult>(result);
            Assert.Equal(201, statusResult.StatusCode);

            var apiResponse = Assert.IsType<ApiResponse<ProductResponse>>(statusResult.Value);
            Assert.True(apiResponse.Success);
            Assert.Equal(201, apiResponse.Code);
            Assert.NotNull(apiResponse.Data);
            Assert.Equal("Sữa tươi Vinamilk 1L", apiResponse.Data!.Name);
            Assert.Equal("Sữa tươi", apiResponse.Data.CategoryName);
            Assert.StartsWith("/uploads/products/", apiResponse.Data.ImageUrl);
            Assert.EndsWith("_vinamilk.png", apiResponse.Data.ImageUrl);

            var savedProduct = await _context.Products.FirstOrDefaultAsync(p => p.Sku == "MILK-1L");
            Assert.NotNull(savedProduct);

            var expectedPhysicalPath = Path.Combine(
                _tempWebRootPath,
                apiResponse.Data.ImageUrl!.TrimStart('/').Replace('/', Path.DirectorySeparatorChar)
            );
            Assert.True(File.Exists(expectedPhysicalPath));
        }

        [Fact]
        public async Task CreateProduct_InvalidModelState_Returns400BadRequest()
        {
            // Arrange
            _controller.ModelState.AddModelError("ProductName", "Tên sản phẩm không được bỏ trống");
            var dto = new CreateRequest();

            // Act
            var result = await _controller.CreateProduct(dto);

            // Assert
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            Assert.Equal(400, badRequestResult.StatusCode);

            var apiResponse = Assert.IsType<ApiResponse<object>>(badRequestResult.Value);
            Assert.False(apiResponse.Success);
            Assert.Equal("Dữ liệu nhập vào không hợp lệ!", apiResponse.Message);
            Assert.NotNull(apiResponse.Errors);
        }

        [Fact]
        public async Task CreateProduct_DuplicateSku_Returns400BadRequest()
        {
            // Arrange
            _context.Categories.Add(new Category { CategoryId = 1, CategoryName = "Gia vị" });
            _context.Products.Add(new Product
            {
                ProductId = 1,
                CategoryId = 1,
                ProductName = "Nước mắm Nam Ngư",
                Sku = "SKU-DUPLICATE",
                SellingPrice = 20000,
                Status = true
            });
            await _context.SaveChangesAsync();

            var dto = new CreateRequest
            {
                ProductName = "Nước mắm mới",
                CategoryId = 1,
                Sku = "SKU-DUPLICATE",
                SellingPrice = 22000,
                ImageFile = CreateMockFormFile("test.jpg")
            };

            // Act
            var result = await _controller.CreateProduct(dto);

            // Assert
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            var apiResponse = Assert.IsType<ApiResponse<object>>(badRequestResult.Value);
            Assert.False(apiResponse.Success);
            Assert.Contains("SKU-DUPLICATE", apiResponse.Message);
        }

        [Fact]
        public async Task CreateProduct_CategoryDoesNotExist_Returns400BadRequest()
        {
            // Arrange
            var dto = new CreateRequest
            {
                ProductName = "Mì Hảo Hảo",
                CategoryId = 999,
                Sku = "MI-001",
                SellingPrice = 4500,
                ImageFile = CreateMockFormFile("mi.jpg")
            };

            // Act
            var result = await _controller.CreateProduct(dto);

            // Assert
            var badRequestResult = Assert.IsType<BadRequestObjectResult>(result);
            var apiResponse = Assert.IsType<ApiResponse<object>>(badRequestResult.Value);
            Assert.False(apiResponse.Success);
            Assert.Contains("999", apiResponse.Message);
        }
    }
}
