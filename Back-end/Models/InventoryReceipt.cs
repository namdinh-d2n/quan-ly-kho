using SupermarketAPI.Models.Auth;
using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class InventoryReceipt
{
    public long Id { get; set; }

    public string ReceiptCode { get; set; } = null!;

    public string ReceiptType { get; set; } = null!;

    public string Status { get; set; } = null!;

    public int? SupplierId { get; set; }

    public string? DestinationLocation { get; set; }

    public int CreatedBy { get; set; }

    public int? ApprovedBy { get; set; }

    public DateTime? ApprovedAt { get; set; }

    public string? RejectionReason { get; set; }

    public string? Note { get; set; }

    public int TotalItems { get; set; }

    public int TotalQuantity { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }

    public virtual User? ApprovedByNavigation { get; set; }

    public virtual User CreatedByNavigation { get; set; } = null!;

    public virtual ICollection<ReceiptDetail> ReceiptDetails { get; set; } = new List<ReceiptDetail>();

    public virtual Supplier? Supplier { get; set; }
}
