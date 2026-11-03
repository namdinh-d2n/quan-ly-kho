using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class ReceiptDetail
{
    public long Id { get; set; }

    public long ReceiptId { get; set; }

    public int ProductId { get; set; }

    public string Sku { get; set; } = null!;

    public string ProductName { get; set; } = null!;

    public int Quantity { get; set; }

    public decimal? UnitPrice { get; set; }

    public DateOnly? ExpiryDate { get; set; }

    public int? AvailableStock { get; set; }

    public virtual Product Product { get; set; } = null!;

    public virtual InventoryReceipt Receipt { get; set; } = null!;
}
