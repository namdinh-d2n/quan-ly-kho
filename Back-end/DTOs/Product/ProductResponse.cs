using System;

namespace SupermarketAPI.DTOs.Product
{
    public class ProductResponse
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string Sku { get; set; } = string.Empty;
        public string? Barcode { get; set; }
        public string? Unit { get; set; }

        public int CategoryId { get; set; }
        public string CategoryName { get; set; } = string.Empty;

        public decimal Price { get; set; }
        public string PriceFormatted => $"{Price:N0} đ";

        public int Stock { get; set; }
        public int MinStock { get; set; }

        // Cảnh báo tồn kho <= tối thiểu
        public bool IsWarning => Stock <= MinStock;

        public string? ImageUrl { get; set; }
        public bool Status { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}