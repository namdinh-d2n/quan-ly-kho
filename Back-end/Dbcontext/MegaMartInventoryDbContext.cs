using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Models;
using SupermarketAPI.Models.Auth;

namespace SupermarketAPI.Dbcontext;

public partial class MegaMartInventoryDbContext : DbContext
{
    public MegaMartInventoryDbContext()
    {
    }

    public MegaMartInventoryDbContext(DbContextOptions<MegaMartInventoryDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Category> Categories { get; set; }

    public virtual DbSet<InventoryReceipt> InventoryReceipts { get; set; }

    public virtual DbSet<Product> Products { get; set; }

    public virtual DbSet<ReceiptDetail> ReceiptDetails { get; set; }

    public virtual DbSet<StocktakeDetail> StocktakeDetails { get; set; }

    public virtual DbSet<StocktakeSession> StocktakeSessions { get; set; }

    public virtual DbSet<Supplier> Suppliers { get; set; }

    public virtual DbSet<User> Users { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Category>(entity =>
        {
            entity.Property(e => e.CategoryId).HasColumnName("CategoryID");
            entity.Property(e => e.CategoryName).HasMaxLength(100);
        });

        modelBuilder.Entity<InventoryReceipt>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("pk_inventory_receipts");

            entity.ToTable("inventory_receipts");

            entity.HasIndex(e => e.ReceiptCode, "uq_inventory_receipts_receipt_code").IsUnique();

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.ApprovedAt)
                .HasColumnType("datetime")
                .HasColumnName("approved_at");
            entity.Property(e => e.ApprovedBy).HasColumnName("approved_by");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("(getdate())", "df_inventory_receipts_created_at")
                .HasColumnType("datetime")
                .HasColumnName("created_at");
            entity.Property(e => e.CreatedBy).HasColumnName("created_by");
            entity.Property(e => e.DestinationLocation)
                .HasMaxLength(100)
                .HasColumnName("destination_location");
            entity.Property(e => e.Note).HasColumnName("note");
            entity.Property(e => e.ReceiptCode)
                .HasMaxLength(30)
                .IsUnicode(false)
                .HasColumnName("receipt_code");
            entity.Property(e => e.ReceiptType)
                .HasMaxLength(10)
                .IsUnicode(false)
                .HasColumnName("receipt_type");
            entity.Property(e => e.RejectionReason).HasColumnName("rejection_reason");
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("PENDING", "df_inventory_receipts_status")
                .HasColumnName("status");
            entity.Property(e => e.SupplierId).HasColumnName("supplier_id");
            entity.Property(e => e.TotalItems).HasColumnName("total_items");
            entity.Property(e => e.TotalQuantity).HasColumnName("total_quantity");
            entity.Property(e => e.UpdatedAt)
                .HasDefaultValueSql("(getdate())", "df_inventory_receipts_updated_at")
                .HasColumnType("datetime")
                .HasColumnName("updated_at");

            entity.HasOne(d => d.ApprovedByNavigation).WithMany(p => p.InventoryReceiptApprovedByNavigations)
                .HasForeignKey(d => d.ApprovedBy)
                .HasConstraintName("fk_inventory_receipts_users_approved");

            entity.HasOne(d => d.CreatedByNavigation).WithMany(p => p.InventoryReceiptCreatedByNavigations)
                .HasForeignKey(d => d.CreatedBy)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("fk_inventory_receipts_users_created");

            entity.HasOne(d => d.Supplier).WithMany(p => p.InventoryReceipts)
                .HasForeignKey(d => d.SupplierId)
                .HasConstraintName("fk_inventory_receipts_suppliers");
        });

        modelBuilder.Entity<Product>(entity =>
        {
            entity.HasIndex(e => e.Sku, "UQ_Products_SKU").IsUnique();

            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.Barcode)
                .HasMaxLength(100)
                .IsUnicode(false);
            entity.Property(e => e.CategoryId).HasColumnName("CategoryID");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("(getdate())", "DF_Products_CreatedAt")
                .HasColumnType("datetime");
            entity.Property(e => e.ImageUrl)
                .HasMaxLength(500)
                .IsUnicode(false);
            entity.Property(e => e.ImportPrice).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.ProductName).HasMaxLength(200);
            entity.Property(e => e.SellingPrice).HasColumnType("decimal(18, 2)");
            entity.Property(e => e.StockQuantity).HasDefaultValue(0);
            entity.Property(e => e.MinStockLevel).HasDefaultValue(0);
            entity.Property(e => e.Sku)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("SKU");
            entity.Property(e => e.Status).HasDefaultValue(true, "DF_Products_Status");
            entity.Property(e => e.Unit).HasMaxLength(50);
            entity.Property(e => e.UpdatedAt)
                .HasDefaultValueSql("(getdate())", "DF_Products_UpdatedAt")
                .HasColumnType("datetime");

            entity.HasOne(d => d.Category).WithMany(p => p.Products)
                .HasForeignKey(d => d.CategoryId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_Products_Categories");
        });

        modelBuilder.Entity<ReceiptDetail>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("pk_receipt_details");

            entity.ToTable("receipt_details");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.AvailableStock).HasColumnName("available_stock");
            entity.Property(e => e.ExpiryDate).HasColumnName("expiry_date");
            entity.Property(e => e.ProductId).HasColumnName("product_id");
            entity.Property(e => e.ProductName)
                .HasMaxLength(255)
                .HasColumnName("product_name");
            entity.Property(e => e.Quantity).HasColumnName("quantity");
            entity.Property(e => e.ReceiptId).HasColumnName("receipt_id");
            entity.Property(e => e.Sku)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("sku");
            entity.Property(e => e.UnitPrice)
                .HasColumnType("decimal(18, 2)")
                .HasColumnName("unit_price");

            entity.HasOne(d => d.Product).WithMany(p => p.ReceiptDetails)
                .HasForeignKey(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("fk_receipt_details_products");

            entity.HasOne(d => d.Receipt).WithMany(p => p.ReceiptDetails)
                .HasForeignKey(d => d.ReceiptId)
                .HasConstraintName("fk_receipt_details_inventory_receipts");
        });

        modelBuilder.Entity<StocktakeDetail>(entity =>
        {
            entity.HasKey(e => e.DetailId);

            entity.Property(e => e.DetailId).HasColumnName("DetailID");
            entity.Property(e => e.Difference).HasComputedColumnSql("([ActualQuantity]-[SystemQuantity])", false);
            entity.Property(e => e.Note).HasMaxLength(255);
            entity.Property(e => e.ProductId).HasColumnName("ProductID");
            entity.Property(e => e.ProductName).HasMaxLength(255);
            entity.Property(e => e.SessionId).HasColumnName("SessionID");
            entity.Property(e => e.Sku)
                .HasMaxLength(50)
                .IsUnicode(false)
                .HasColumnName("SKU");

            entity.HasOne(d => d.Product).WithMany(p => p.StocktakeDetails)
                .HasForeignKey(d => d.ProductId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_StocktakeDetails_Products");

            entity.HasOne(d => d.Session).WithMany(p => p.StocktakeDetails)
                .HasForeignKey(d => d.SessionId)
                .HasConstraintName("FK_StocktakeDetails_StocktakeSessions");
        });

        modelBuilder.Entity<StocktakeSession>(entity =>
        {
            entity.HasKey(e => e.SessionId);

            entity.HasIndex(e => e.Code, "UQ_StocktakeSessions_Code").IsUnique();

            entity.Property(e => e.SessionId).HasColumnName("SessionID");
            entity.Property(e => e.Code)
                .HasMaxLength(30)
                .IsUnicode(false);
            entity.Property(e => e.CompletedAt).HasColumnType("datetime");
            entity.Property(e => e.CreatedAt)
                .HasDefaultValueSql("(getdate())", "DF_StocktakeSessions_CreatedAt")
                .HasColumnType("datetime");
            entity.Property(e => e.Name).HasMaxLength(255);
            entity.Property(e => e.Status)
                .HasMaxLength(20)
                .IsUnicode(false)
                .HasDefaultValue("Open", "DF_StocktakeSessions_Status");

            entity.HasOne(d => d.AssignedToNavigation).WithMany(p => p.StocktakeSessionAssignedToNavigations)
                .HasForeignKey(d => d.AssignedTo)
                .HasConstraintName("FK_StocktakeSessions_Users_Assigned");

            entity.HasOne(d => d.CreatedByNavigation).WithMany(p => p.StocktakeSessionCreatedByNavigations)
                .HasForeignKey(d => d.CreatedBy)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK_StocktakeSessions_Users_Created");
        });

        modelBuilder.Entity<Supplier>(entity =>
        {
            entity.Property(e => e.SupplierId).HasColumnName("SupplierID");
            entity.Property(e => e.Address).HasMaxLength(255);
            entity.Property(e => e.ContactName).HasMaxLength(100);
            entity.Property(e => e.Phone)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.SupplierName).HasMaxLength(200);
        });

        modelBuilder.Entity<User>(entity =>
        {
            entity.Property(e => e.UserId).HasColumnName("UserID");
            entity.Property(e => e.FullName).HasMaxLength(100);
            entity.Property(e => e.PasswordHash)
                .HasMaxLength(255)
                .IsUnicode(false);
            entity.Property(e => e.Phone_Email)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("Phone_Email");
            entity.Property(e => e.Role)
                .HasMaxLength(20)
                .IsUnicode(false);
            entity.Property(e => e.Status).HasDefaultValue(true, "DF_Users_Status");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
