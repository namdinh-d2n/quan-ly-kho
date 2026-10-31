using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Models;
using SupermarketAPI.Models.Auth;

namespace SupermarketAPI.Dbcontext
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasKey(e => e.UserID);
                entity.Property(e => e.Phone_Email).HasColumnName("Phone_Email");
            });
        }
    }
}
