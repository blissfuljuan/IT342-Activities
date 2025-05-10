# 📇 Google Contacts Manager

<div align="center">

![Google Contacts](https://img.shields.io/badge/Google-Contacts-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring-Boot-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-Secured-FF4B4B?style=for-the-badge&logo=auth0&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

---

<p align="center">
  <img src="https://raw.githubusercontent.com/ejaysol/IT342-Activities-Solasco/main/screenshots/contacts-dark.png" alt="Dark Mode" width="45%">
  <img src="https://raw.githubusercontent.com/ejaysol/IT342-Activities-Solasco/main/screenshots/contacts-light.png" alt="Light Mode" width="45%">
</p>

*A sleek and modern Google Contacts manager with dark/light theme support*

</div>

## ✨ Features

<div align="center">

| Feature | Description |
|---------|-------------|
| 🎨 **Modern UI** | Beautiful interface with animated backgrounds |
| 🌓 **Theme Support** | Toggle between dark and light modes |
| 📱 **Responsive** | Works seamlessly on all devices |
| 🔍 **Smart Search** | Real-time contact filtering |
| 📊 **Contact Management** | Add, edit, and delete contacts easily |
| 🔐 **Secure** | OAuth2 authentication with Google |

</div>

## 🚀 Quick Start

### Prerequisites

```bash
✓ Java 17+
✓ Maven
✓ Google Cloud Account
✓ Your Favorite IDE
```

### 🔑 Environment Setup

```bash
# Set your Google OAuth credentials
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret
```

### 🛠️ Installation

```bash
# Clone the repository
git clone https://github.com/ejaysol/IT342-Activities-Solasco.git

# Navigate to project directory
cd IT342-Activities-Solasco/googlecontact

# Install dependencies
mvn install

# Run the application
mvn spring-boot:run
```

## 📋 Requirements

### System Requirements
- Windows 10/11, macOS, or Linux
- Minimum 4GB RAM
- 1GB free disk space
- Internet connection for Google API access

### Software Requirements
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.6+
- Modern web browser (Chrome, Firefox, Edge)
- Git (for cloning the repository)

### Google Cloud Setup Requirements
1. Google Cloud Account
2. Google Cloud Project with:
   - Google People API enabled
   - OAuth 2.0 credentials configured
   - Authorized redirect URIs set up

## 🚀 Installation Guide

### Step 1: Prerequisites Setup

1. **Install Java 17+**
   ```bash
   # Check Java version
   java -version
   
   # Should output version 17 or higher
   ```

2. **Install Maven**
   ```bash
   # Check Maven version
   mvn -version
   
   # Should output version 3.6 or higher
   ```

3. **Install Git**
   ```bash
   # Check Git version
   git --version
   ```

### Step 2: Google Cloud Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing one
3. Enable the Google People API:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Google People API"
   - Click "Enable"

4. Create OAuth 2.0 credentials:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth client ID"
   - Select "Web application"
   - Add authorized redirect URIs:
     ```
     http://localhost:8080/login/oauth2/code/google
     ```
   - Save your Client ID and Client Secret

### Step 3: Application Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/ejaysol/IT342-Activities-Solasco.git
   cd IT342-Activities-Solasco/googlecontact
   ```

2. **Configure Environment Variables**
   
   Windows (PowerShell):
   ```powershell
   $env:GOOGLE_CLIENT_ID="your_client_id"
   $env:GOOGLE_CLIENT_SECRET="your_client_secret"
   ```

   Linux/macOS:
   ```bash
   export GOOGLE_CLIENT_ID="your_client_id"
   export GOOGLE_CLIENT_SECRET="your_client_secret"
   ```

3. **Build the Application**
   ```bash
   mvn clean install
   ```

### Step 4: Running the Application

1. **Start the Server**
   ```bash
   mvn spring-boot:run
   ```

2. **Access the Application**
   - Open your web browser
   - Navigate to: `http://localhost:8080`
   - Log in with your Google account

## 🔍 Verifying Installation

1. **Check Server Status**
   - Server should start without errors
   - Look for "Started Application" message in console

2. **Verify Google Integration**
   - Click "Login with Google"
   - Should redirect to Google login
   - After login, should see your contacts

3. **Test Core Features**
   - Try adding a new contact
   - Search for existing contacts
   - Edit a contact
   - Toggle dark/light theme

## ❗ Common Issues and Solutions

### Server Won't Start
- Check if port 8080 is available
- Verify Java version is 17+
- Ensure Maven is properly installed

### Google Login Failed
- Verify OAuth credentials are correct
- Check redirect URIs in Google Console
- Ensure environment variables are set

### Contact List Empty
- Verify Google People API is enabled
- Check OAuth scopes include contacts
- Ensure Google account has contacts

## 📞 Support

If you encounter any issues:
1. Check the logs in the console
2. Verify all requirements are met
3. Ensure Google Cloud setup is correct
4. Create an issue on GitHub with:
   - Error message
   - Steps to reproduce
   - System information

## 🎯 Core Features

### Contact Management
- **📝 Add Contacts**
  - Multiple email addresses
  - Various phone number types
  - Smart form validation
  
- **🔄 Edit Contacts**
  - Intuitive interface
  - Real-time updates
  - Field type management

- **🔍 Search Functionality**
  - Instant results
  - Filter by name/email/phone
  - Smart highlighting

### UI/UX Features
- **🎨 Theme Switching**
  - System theme detection
  - Smooth transitions
  - Persistent preferences

- **💫 Animations**
  - Gradient backgrounds
  - Floating contact cards
  - Smooth interactions

- **📱 Responsive Design**
  - Mobile-first approach
  - Adaptive layouts
  - Touch-friendly interface

## 🛡️ Security Features

- **OAuth2 Integration**
  - Secure Google authentication
  - Token management
  - Scope-based access

- **Data Protection**
  - CSRF protection
  - Secure sessions
  - Environment variables

## 🎨 Theme Preview

<div align="center">

### 🌙 Dark Mode
Perfect for night-time usage with carefully selected colors

### ☀️ Light Mode
Clean and crisp interface for daytime productivity

</div>

## 🔧 Technical Stack

<div align="center">

| Frontend | Backend | Security |
|----------|---------|----------|
| HTML5 | Spring Boot | OAuth2 |
| CSS3 | Java 17 | CSRF Protection |
| JavaScript | Maven | Session Management |
| Bootstrap 5 | Google API | Environment Config |
| Thymeleaf | REST Services | Spring Security |

</div>

## 📚 Documentation

- [Setup Guide](docs/setup.md)
- [API Documentation](docs/api.md)
- [User Guide](docs/user-guide.md)

## 👨‍💻 Author

<div align="center">

**Ephraim Jay Solasco**

[![GitHub](https://img.shields.io/badge/GitHub-ejaysol-181717?style=for-the-badge&logo=github)](https://github.com/ejaysol)

</div>

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

Made with ❤️ using Spring Boot and Google APIs

</div>