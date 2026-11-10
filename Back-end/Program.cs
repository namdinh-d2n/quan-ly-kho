
using System;
using System.Collections.Generic;
using System.Text;

using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;

using SupermarketAPI.Dbcontext;

var builder = WebApplication.CreateBuilder(args);

// =====================================================
// 1. ĐĂNG KÝ DBCONTEXT - KẾT NỐI SQL SERVER
// =====================================================
builder.Services.AddDbContext<MegaMartInventoryDbContext>(options =>
    options.UseSqlServer(
        builder.Configuration.GetConnectionString("DefaultConnection")
    )
);

// =====================================================
// 2. CẤU HÌNH JWT AUTHENTICATION
// =====================================================
var jwtSettings = builder.Configuration.GetSection("JwtSettings");

var secretKey = jwtSettings["SecretKey"]
    ?? "MegaMartSecretKeyForJWTAuth123456SecureKey";

var key = Encoding.UTF8.GetBytes(secretKey);

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme =
        JwtBearerDefaults.AuthenticationScheme;

    options.DefaultChallengeScheme =
        JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),

        ValidateIssuer = false,
        ValidateAudience = false,

        ClockSkew = TimeSpan.Zero
    };
});

// =====================================================
// 3. ĐĂNG KÝ AUTHORIZATION - PHÂN QUYỀN THEO ROLE
// =====================================================
builder.Services.AddAuthorization();

// =====================================================
// 4. ĐĂNG KÝ CONTROLLERS
// =====================================================
builder.Services.AddControllers();

// =====================================================
// 5. CẤU HÌNH SWAGGER VÀ JWT BEARER
// =====================================================
builder.Services.AddEndpointsApiExplorer();

builder.Services.AddSwaggerGen(options =>
{
    // Khai báo phương thức xác thực JWT Bearer
    options.AddSecurityDefinition("Bearer",
        new OpenApiSecurityScheme
        {
            Name = "Authorization",
            Type = SecuritySchemeType.Http,
            Scheme = "bearer",
            BearerFormat = "JWT",
            In = ParameterLocation.Header,
            Description = "Nhập JWT Token để xác thực API."
        });

    // Thiết lập JWT Security Requirement
    options.AddSecurityRequirement(document =>
        new OpenApiSecurityRequirement
        {
            [
                new OpenApiSecuritySchemeReference(
                    "Bearer",
                    document
                )
            ] = new List<string>()
        }
    );
});

// =====================================================
// 6. CẤU HÌNH CORS
// =====================================================
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy
            .AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader();
    });
});

// =====================================================
// 7. BUILD APPLICATION
// =====================================================
var app = builder.Build();

// =====================================================
// 8. CẤU HÌNH SWAGGER
// =====================================================
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// =====================================================
// 9. CẤU HÌNH MIDDLEWARE
// =====================================================

// Tạm không chuyển hướng HTTPS trong môi trường HTTP
// app.UseHttpsRedirection();

// CORS
app.UseCors("AllowAll");

// Authentication phải chạy trước Authorization
app.UseAuthentication();

app.UseAuthorization();

// =====================================================
// 10. ĐĂNG KÝ ROUTE CONTROLLERS
// =====================================================
app.MapControllers();

// =====================================================
// 11. KHỞI CHẠY APPLICATION
// =====================================================
app.Run();
