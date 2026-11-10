using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace SupermarketAPI.DTOs.Product
{
    public class CreateRequest
    {
        [Required(ErrorMessage = "Tên sản phẩm không được bỏ trống")]
        [StringLength(200, ErrorMessage = "Tên sản phẩm không được vượt quá 200 ký tự")]
        public string ProductName { get; set; } = string.Empty;

        [Required(ErrorMessage = "Danh mục sản phẩm là bắt buộc")]
        [Range(1, int.MaxValue, ErrorMessage = "Danh mục sản phẩm không hợp lệ")]
        public int CategoryId { get; set; }

        [Required(ErrorMessage = "Mã SKU không được bỏ trống")]
        [StringLength(50, ErrorMessage = "Mã SKU không được vượt quá 50 ký tự")]
        public string Sku { get; set; } = string.Empty;

        [StringLength(100)]
        public string? Barcode { get; set; }

        [StringLength(50)]
        public string? Unit { get; set; }

        [Range(0, double.MaxValue, ErrorMessage = "Giá nhập không được nhỏ hơn 0")]
        public decimal ImportPrice { get; set; } = 0;

        [Required(ErrorMessage = "Giá bán là bắt buộc")]
        [Range(0.01, double.MaxValue, ErrorMessage = "Giá bán phải lớn hơn 0")]
        public decimal SellingPrice { get; set; }

        [Range(0, int.MaxValue, ErrorMessage = "Số lượng tồn kho không hợp lệ")]
        public int StockQuantity { get; set; } = 0;

        [Range(0, int.MaxValue, ErrorMessage = "Mức tồn kho tối thiểu không hợp lệ")]
        public int MinStockLevel { get; set; } = 10;

        [Required(ErrorMessage = "Hình ảnh sản phẩm không được bỏ trống!")]
        public IFormFile ImageFile { get; set; } = null!;
    }
}