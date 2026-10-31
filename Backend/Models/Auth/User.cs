namespace SupermarketAPI.Models.Auth
{
    public class User
    {
        public int UserID { get; set; }
        public string FullName { get; set; } = string.Empty;
        public string Phone_Email { get; set; } = string.Empty;
        public string PasswordHash { get; set; } = string.Empty;
        public string Role { get; set; } = string.Empty; 
        public bool Status { get; set; }
    }
}
