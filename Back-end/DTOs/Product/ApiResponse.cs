using System;

namespace SupermarketAPI.DTOs.Product
{
    public class ApiResponse<T>
    {
        public bool Success { get; set; }
        public int Code { get; set; }
        public string Message { get; set; } = string.Empty;
        public T? Data { get; set; }
        public object? Errors { get; set; }
        public DateTime Timestamp { get; set; } = DateTime.UtcNow;

        public static ApiResponse<T> SuccessResult(T data, string message = "Thao tác thành công", int code = 200)
        {
            return new ApiResponse<T>
            {
                Success = true,
                Code = code,
                Message = message,
                Data = data,
                Errors = null,
                Timestamp = DateTime.UtcNow
            };
        }

        public static ApiResponse<T> ErrorResult(string message, object? errors = null, int code = 400)
        {
            return new ApiResponse<T>
            {
                Success = false,
                Code = code,
                Message = message,
                Data = default,
                Errors = errors,
                Timestamp = DateTime.UtcNow
            };
        }
    }
}