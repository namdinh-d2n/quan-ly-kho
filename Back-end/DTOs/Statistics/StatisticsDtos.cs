namespace SupermarketAPI.DTOs.Statistics
{
    // DTO 1: Dữ liệu biểu đồ nhập - xuất
    public class ChartDataPointDto
    {
        public string Label { get; set; } = string.Empty;
        public int TotalImport { get; set; }
        public int TotalExport { get; set; }
    }

    // DTO 2: Top sản phẩm xuất kho nhiều nhất
    public class TopExportedProductDto
    {
        public int ProductId { get; set; }
        public string Sku { get; set; } = string.Empty;
        public string ProductName { get; set; } = string.Empty;
        public string? ImageUrl { get; set; }
        public int TotalExportedQuantity { get; set; }
    }

    // DTO 3: Top sản phẩm tồn kho lâu nhất
    public class OldestStockProductDto
    {
        public int ProductId { get; set; }
        public string Sku { get; set; } = string.Empty;
        public string ProductName { get; set; } = string.Empty;
        public string? ImageUrl { get; set; }
        public int CurrentStock { get; set; }
        public DateTime? OldestImportDate { get; set; }
    }
}