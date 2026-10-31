namespace SupermarketAPI.DTOs.Auth
{
    public class LoginRequest
    {
        public string PhoneEmail { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
    }
}
