using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.DTOs.Product;
using SupermarketAPI.DTOs.Category;

namespace SupermarketAPI.Controllers.Categories
{
    [ApiController]
    [Route("api/v1/categories")]
    public class CategoriesController : ControllerBase
    {
        private readonly MegaMartInventoryDbContext _context;

        public CategoriesController(MegaMartInventoryDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetCategories()
        {
            var categories = await _context.Categories
                .Select(c => new CategoryResponse
                {
                    CategoryId = c.CategoryId,
                    CategoryName = c.CategoryName
                })
                .ToListAsync();

            return Ok(ApiResponse<List<CategoryResponse>>.SuccessResult(
                categories,
                "Lấy danh sách danh mục thành công!"
            ));
        }
    }
}