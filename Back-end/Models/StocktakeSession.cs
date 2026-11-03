using SupermarketAPI.Models.Auth;
using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class StocktakeSession
{
    public int SessionId { get; set; }

    public string Code { get; set; } = null!;

    public string Name { get; set; } = null!;

    public string Status { get; set; } = null!;

    public int CreatedBy { get; set; }

    public int? AssignedTo { get; set; }

    public string? Note { get; set; }

    public DateTime CreatedAt { get; set; }

    public DateTime? CompletedAt { get; set; }

    public virtual User? AssignedToNavigation { get; set; }

    public virtual User CreatedByNavigation { get; set; } = null!;

    public virtual ICollection<StocktakeDetail> StocktakeDetails { get; set; } = new List<StocktakeDetail>();
}
