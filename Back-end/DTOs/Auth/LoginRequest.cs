namespace SupermarketAPI.DTOs.Auth
{
    public class LoginRequest
    {
        public string Phone_Email { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }
}
