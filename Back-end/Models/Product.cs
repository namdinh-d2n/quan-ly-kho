using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class Product
{
    public int ProductId { get; set; }

    public int CategoryId { get; set; }

    public string ProductName { get; set; } = null!;

    public string Sku { get; set; } = null!;

    public string? Barcode { get; set; }

    public string? Unit { get; set; }

    public decimal ImportPrice { get; set; }

    public decimal SellingPrice { get; set; }

    public int StockQuantity { get; set; }

    public int MinStockLevel { get; set; }

    public string? ImageUrl { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public bool Status { get; set; }

    public virtual Category Category { get; set; } = null!;

    public virtual ICollection<ReceiptDetail> ReceiptDetails { get; set; } = new List<ReceiptDetail>();

    public virtual ICollection<StocktakeDetail> StocktakeDetails { get; set; } = new List<StocktakeDetail>();
}
