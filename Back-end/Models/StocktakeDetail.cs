using System;
using System.Collections.Generic;

namespace SupermarketAPI.Models;

public partial class StocktakeDetail
{
    public int DetailId { get; set; }

    public int SessionId { get; set; }

    public int ProductId { get; set; }

    public string Sku { get; set; } = null!;

    public string ProductName { get; set; } = null!;

    public int SystemQuantity { get; set; }

    public int? ActualQuantity { get; set; }

    public int? Difference { get; set; }

    public string? Note { get; set; }

    public virtual Product Product { get; set; } = null!;

    public virtual StocktakeSession Session { get; set; } = null!;
}
