using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SupermarketAPI.Dbcontext;
using SupermarketAPI.DTOs.Auth;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace SupermarketAPI.Controllers.Auth
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _configuration;

        public AuthController(AppDbContext context, IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.PhoneEmail) || string.IsNullOrWhiteSpace(request.Password))
                return BadRequest(new { message = "Vui lòng nhập đầy đủ thông tin!" });

            // Tìm user theo Phone hoặc Email
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Phone_Email == request.PhoneEmail);

            if (user == null)
                return Unauthorized(new { message = "Tài khoản hoặc mật khẩu không chính xác!" });

            if (!user.Status)
                return Unauthorized(new { message = "Tài khoản của bạn đã bị khóa!" });
            bool isValidPassword = user.PasswordHash.StartsWith("hash_pass_")
                ? request.Password == user.PasswordHash || request.Password == "123456" // Cho phép test mật khẩu "123456"
                : BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash);

            if (!isValidPassword)
                return Unauthorized(new { message = "Tài khoản hoặc mật khẩu không chính xác!" });

            // Tạo Token JWT chứa Role claim
            var token = GenerateJwtToken(user);
                
            return Ok(new AuthResponse
            {
                Token = token,
                UserID = user.UserID,
                FullName = user.FullName,
                Role = user.Role
            });
        }
    private string GenerateJwtToken(Models.Auth.User user)
        {
            var jwtSettings = _configuration.GetSection("JwtSettings");
            var key = Encoding.UTF8.GetBytes(jwtSettings["SecretKey"] ?? "SuperMarketSecretKeyForJWTAuth123456");

            var claims = new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.UserID.ToString()),
                new Claim(ClaimTypes.Name, user.FullName),
                new Claim(ClaimTypes.Role, user.Role) // Lưu vai trò để phân quyền
            };

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(claims),
                Expires = DateTime.UtcNow.AddDays(7),
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }
    }

}
