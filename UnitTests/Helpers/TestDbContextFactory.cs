using System;
using Microsoft.EntityFrameworkCore;
using SupermarketAPI.Dbcontext;

namespace UnitTests.Helpers
{
    public static class TestDbContextFactory
    {
        /// <summary>
        /// Tạo mới một DbContext InMemory độc lập cho mỗi test case
        /// </summary>
        public static MegaMartInventoryDbContext Create()
        {
            var options = new DbContextOptionsBuilder<MegaMartInventoryDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            return new MegaMartInventoryDbContext(options);
        }
    }
}
