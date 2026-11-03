using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class Supplier
{
    public int SupplierId { get; set; }

    public string SupplierName { get; set; } = null!;

    public string? ContactName { get; set; }

    public string? Phone { get; set; }

    public string? Address { get; set; }

    public virtual ICollection<InventoryReceipt> InventoryReceipts { get; set; } = new List<InventoryReceipt>();
}
