using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using Microsoft.AspNetCore.Http;
using Moq;
using SupermarketAPI.DTOs.Product;
using Xunit;

namespace SupermarketAPI.Tests.DTOs
{
    public class ProductDtoTests
    {
        [Fact]
        public void CreateRequest_MissingRequiredFields_FailsValidation()
        {
            // Arrange
            var dto = new CreateRequest
            {
                ProductName = "",
                CategoryId = 0,
                Sku = "",
                SellingPrice = 0,
                ImageFile = null!
            };

            var validationResults = new List<ValidationResult>();
            var context = new ValidationContext(dto);

            // Act
            var isValid = Validator.TryValidateObject(dto, context, validationResults, validateAllProperties: true);

            // Assert
            Assert.False(isValid);
            Assert.Contains(validationResults, v => v.MemberNames.Contains(nameof(CreateRequest.ProductName)));
            Assert.Contains(validationResults, v => v.MemberNames.Contains(nameof(CreateRequest.CategoryId)));
            Assert.Contains(validationResults, v => v.MemberNames.Contains(nameof(CreateRequest.Sku)));
            Assert.Contains(validationResults, v => v.MemberNames.Contains(nameof(CreateRequest.SellingPrice)));
            Assert.Contains(validationResults, v => v.MemberNames.Contains(nameof(CreateRequest.ImageFile)));
        }

        [Fact]
        public void CreateRequest_AllValidFields_PassesValidation()
        {
            // Arrange
            var dto = new CreateRequest
            {
                ProductName = "Dầu ăn Neptune 1L",
                CategoryId = 2,
                Sku = "NEPTUNE-1L",
                SellingPrice = 55000,
                ImportPrice = 45000,
                StockQuantity = 10,
                MinStockLevel = 5,
                ImageFile = new Mock<IFormFile>().Object
            };

            var validationResults = new List<ValidationResult>();
            var context = new ValidationContext(dto);

            // Act
            var isValid = Validator.TryValidateObject(dto, context, validationResults, validateAllProperties: true);

            // Assert
            Assert.True(isValid);
            Assert.Empty(validationResults);
        }

        [Theory]
        [InlineData(5, 10, true)]
        [InlineData(10, 10, true)]
        [InlineData(11, 10, false)]
        public void ProductResponse_IsWarning_ReturnsExpectedResult(int stock, int minStock, bool expectedWarning)
        {
            // Arrange
            var response = new ProductResponse
            {
                Stock = stock,
                MinStock = minStock
            };

            // Act & Assert
            Assert.Equal(expectedWarning, response.IsWarning);
        }
    }
}
