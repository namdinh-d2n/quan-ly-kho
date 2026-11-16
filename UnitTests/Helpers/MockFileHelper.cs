using System.IO;
using System.Text;
using System.Threading;
using Microsoft.AspNetCore.Http;
using Moq;

namespace UnitTests.Helpers
{
    public static class MockFileHelper
    {
        /// <summary>
        /// Giả lập file upload IFormFile dùng chung cho các API có upload ảnh/tài liệu
        /// </summary>
        public static IFormFile CreateFormFile(string fileName = "test.png", string content = "fake-image-bytes")
        {
            var bytes = Encoding.UTF8.GetBytes(content);
            var stream = new MemoryStream(bytes);
            var mockFile = new Mock<IFormFile>();

            mockFile.Setup(f => f.FileName).Returns(fileName);
            mockFile.Setup(f => f.Length).Returns(bytes.Length);
            mockFile.Setup(f => f.OpenReadStream()).Returns(stream);
            mockFile.Setup(f => f.CopyToAsync(It.IsAny<Stream>(), It.IsAny<CancellationToken>()))
                .Returns((Stream target, CancellationToken token) =>
                {
                    stream.Position = 0;
                    return stream.CopyToAsync(target, token);
                });

            return mockFile.Object;
        }
    }
}
