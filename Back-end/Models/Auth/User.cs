using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models.Auth;

public partial class User
{
    public int UserId { get; set; }

    public string FullName { get; set; } = null!;

    public string Phone_Email { get; set; } = null!;

    public string PasswordHash { get; set; } = null!;

    public string Role { get; set; } = null!;

    public bool Status { get; set; }

    public virtual ICollection<InventoryReceipt> InventoryReceiptApprovedByNavigations { get; set; } = new List<InventoryReceipt>();

    public virtual ICollection<InventoryReceipt> InventoryReceiptCreatedByNavigations { get; set; } = new List<InventoryReceipt>();

    public virtual ICollection<StocktakeSession> StocktakeSessionAssignedToNavigations { get; set; } = new List<StocktakeSession>();

    public virtual ICollection<StocktakeSession> StocktakeSessionCreatedByNavigations { get; set; } = new List<StocktakeSession>();
}
