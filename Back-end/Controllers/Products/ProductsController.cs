using System;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.DTOs.Product;
using SupermarketAPI.Models;

namespace SupermarketAPI.Controllers.Products
{
    [ApiController]
    [Route("api/v1/products")]
    public class ProductsController : ControllerBase
    {
        private readonly MegaMartInventoryDbContext _context;
        private readonly IWebHostEnvironment _env;

        public ProductsController(MegaMartInventoryDbContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        /// <summary>
        /// ST-2.01.4: Lấy danh sách sản phẩm (Mới nhất lên đầu, lọc sản phẩm đang kinh doanh)
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetProducts([FromQuery] string? search = null)
        {
            var query = _context.Products
                .Where(p => p.Status == true) // Chỉ lấy các sản phẩm đang kinh doanh
                .AsNoTracking();

            if (!string.IsNullOrWhiteSpace(search))
            {
                var keyword = search.Trim();
                query = query.Where(p => p.ProductName.Contains(keyword) ||
                                         p.Sku.Contains(keyword) ||
                                         (p.Barcode != null && p.Barcode.Contains(keyword)));
            }

            var products = await query
                .OrderByDescending(p => p.CreatedAt) // Sắp xếp sản phẩm mới tạo lên đầu
                .Select(p => new ProductResponse
                {
                    Id = p.ProductId,
                    Name = p.ProductName,
                    Sku = p.Sku,
                    Barcode = p.Barcode,
                    Unit = p.Unit,
                    CategoryId = p.CategoryId,
                    CategoryName = p.Category != null ? p.Category.CategoryName : "Khác",
                    Price = p.SellingPrice,
                    Stock = p.StockQuantity,
                    MinStock = p.MinStockLevel,
                    ImageUrl = p.ImageUrl,
                    Status = p.Status,
                    CreatedAt = p.CreatedAt
                })
                .ToListAsync();

            return Ok(ApiResponse<object>.SuccessResult(new
            {
                totalItems = products.Count,
                items = products
            }, "Lấy danh sách sản phẩm thành công!"));
        }

        /// <summary>
        /// ST-2.01.2 & ST-2.01.3: Thêm sản phẩm mới + Upload ảnh + Validation
        /// </summary>
        [HttpPost]
        public async Task<IActionResult> CreateProduct([FromForm] CreateRequest dto)
        {
            if (!ModelState.IsValid)
            {
                var validationErrors = ModelState
                    .Where(x => x.Value?.Errors.Count > 0)
                    .Select(x => new
                    {
                        field = x.Key,
                        message = x.Value?.Errors.FirstOrDefault()?.ErrorMessage
                    });

                return BadRequest(ApiResponse<object>.ErrorResult(
                    "Dữ liệu nhập vào không hợp lệ!",
                    validationErrors,
                    400
                ));
            }

            // Kiểm tra trùng Mã SKU
            var isSkuExists = await _context.Products.AnyAsync(p => p.Sku == dto.Sku);
            if (isSkuExists)
            {
                return BadRequest(ApiResponse<object>.ErrorResult(
                    $"Mã SKU '{dto.Sku}' đã tồn tại trong hệ thống!", null, 400
                ));
            }

            // Kiểm tra tồn tại Danh mục
            var categoryExists = await _context.Categories.AnyAsync(c => c.CategoryId == dto.CategoryId);
            if (!categoryExists)
            {
                return BadRequest(ApiResponse<object>.ErrorResult(
                    $"Danh mục với ID = {dto.CategoryId} không tồn tại trong hệ thống!", null, 400
                ));
            }

            // Xử lý Upload file ảnh vào wwwroot/uploads/products
            string? imageUrl = null;
            if (dto.ImageFile != null && dto.ImageFile.Length > 0)
            {
                var uploadsFolder = Path.Combine(_env.WebRootPath ?? Path.Combine(Directory.GetCurrentDirectory(), "wwwroot"), "uploads", "products");
                if (!Directory.Exists(uploadsFolder))
                {
                    Directory.CreateDirectory(uploadsFolder);
                }

                var uniqueFileName = $"{Guid.NewGuid()}_{Path.GetFileName(dto.ImageFile.FileName)}";
                var filePath = Path.Combine(uploadsFolder, uniqueFileName);

                using (var fileStream = new FileStream(filePath, FileMode.Create))
                {
                    await dto.ImageFile.CopyToAsync(fileStream);
                }

                imageUrl = $"/uploads/products/{uniqueFileName}";
            }

            var product = new Product
            {
                CategoryId = dto.CategoryId,
                ProductName = dto.ProductName,
                Sku = dto.Sku,
                Barcode = dto.Barcode,
                Unit = dto.Unit,
                ImportPrice = dto.ImportPrice,
                SellingPrice = dto.SellingPrice,
                StockQuantity = dto.StockQuantity,
                MinStockLevel = dto.MinStockLevel,
                ImageUrl = imageUrl,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow,
                Status = true
            };

            _context.Products.Add(product);
            await _context.SaveChangesAsync();

            var categoryName = await _context.Categories
                .Where(c => c.CategoryId == dto.CategoryId)
                .Select(c => c.CategoryName)
                .FirstOrDefaultAsync() ?? "Khác";

            var resultDto = new ProductResponse
            {
                Id = product.ProductId,
                Name = product.ProductName,
                Sku = product.Sku,
                Barcode = product.Barcode,
                Unit = product.Unit,
                CategoryId = product.CategoryId,
                CategoryName = categoryName,
                Price = product.SellingPrice,
                Stock = product.StockQuantity,
                MinStock = product.MinStockLevel,
                ImageUrl = product.ImageUrl,
                Status = product.Status,
                CreatedAt = product.CreatedAt
            };

            return StatusCode(201, ApiResponse<ProductResponse>.SuccessResult(
                resultDto,
                "Thêm sản phẩm mới thành công!",
                201
            ));
        }
    }
}