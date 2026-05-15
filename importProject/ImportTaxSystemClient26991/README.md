# Import Tax Management System - Client

## Overview

A professional Java Swing client application for the distributed **Import Tax Management System**. Built with Java 17, Maven, FlatLaf, MigLayout, and Java RMI for enterprise-level tax processing and management.

## Project Structure

```
ImportTaxSystemClient26991/
├── pom.xml                                          # Maven configuration
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── importtax/
│       │           └── client/
│       │               ├── AppLauncher.java          # Application entry point
│       │               ├── ui/                       # UI Components
│       │               │   ├── LoginFrame.java       # Login interface
│       │               │   └── DashboardFrame.java   # Main dashboard
│       │               ├── rmi/                      # RMI utilities
│       │               │   └── RmiConnection.java    # RMI connection manager
│       │               └── util/                     # Utility classes
│       │                   ├── UIConstants.java      # UI configuration constants
│       │                   ├── RoundedPanel.java     # Custom rounded panel
│       │                   └── RoundedButton.java    # Custom rounded button
│       └── resources/
│           └── logback.xml                           # Logging configuration
└── README.md                                         # This file
```

## Technology Stack

- **Language**: Java 17
- **Build Tool**: Maven 3.8.1+
- **UI Framework**: Swing with FlatLaf 3.4.1
- **Layout Manager**: MigLayout 11.3
- **Logging**: SLF4J 2.0.13 + Logback 1.5.6
- **Remote Communication**: Java RMI
- **Theme**: FlatLaf Dark (Enterprise-grade)

## Dependencies

### Key Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| FlatLaf | 3.4.1 | Modern UI Look & Feel |
| FlatLaf IntelliJ Themes | 3.4.1 | Professional themes |
| MigLayout Swing | 11.3 | Responsive layout management |
| SLF4J API | 2.0.13 | Logging facade |
| Logback Classic | 1.5.6 | Logging implementation |
| Logback Core | 1.5.6 | Logging core |

## Features

### Current Implementation

✅ **Professional UI Components**
- Modern dark theme with FlatLaf
- Rounded buttons and panels with hover effects
- Responsive layouts using MigLayout
- Professional color scheme and typography

✅ **Login Module**
- Beautiful login interface with logo section
- Username and password fields with focus effects
- Form validation (empty field checks)
- Enter key support for quick login
- Success/error message display
- Keyboard shortcuts (Enter to login, Escape to exit)

✅ **Dashboard Module**
- Sidebar navigation with 8 navigation items
- System header with greeting and live clock
- Dashboard overview with 4 metric cards
- System summary section
- Responsive grid layout
- Logout functionality

✅ **RMI Integration**
- RmiConnection utility for service lookup
- Connection pooling and error handling
- Generic service lookup with type safety
- Connection status checking
- Logging of all RMI operations

✅ **Logging**
- SLF4J with Logback implementation
- Console and file output
- Separate error log file
- Rolling file management (max 10MB, 7-day retention)
- Configurable logging levels

✅ **Enterprise Architecture**
- Clean separation of concerns (UI, RMI, Util)
- Comprehensive JavaDoc documentation
- Java 17 compatible code
- Maven assembly plugin for executable JAR
- Professional error handling

## Installation & Setup

### Prerequisites

- **Java 17** or higher
- **Maven 3.8.1** or higher

### Build Instructions

1. **Navigate to project directory**
   ```bash
   cd ImportTaxSystemClient26991
   ```

2. **Clean and build project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="com.importtax.client.AppLauncher"
   
   # Or run the JAR directly
   java -jar target/ImportTaxClient-jar-with-dependencies.jar
   ```

### Maven Commands

```bash
# Compile only
mvn compile

# Run tests (if added)
mvn test

# Package without running tests
mvn package -DskipTests

# Clean build artifacts
mvn clean

# Check for dependency updates
mvn versions:display-dependency-updates

# Update Maven dependencies
mvn dependency:tree
```

## Configuration

### RMI Configuration

The RMI connection is configured in `RmiConnection.java`:

```java
private static final String RMI_HOST = "localhost";
private static final int RMI_PORT = 5000;
private static final String RMI_URL = "rmi://localhost:5000/";
```

**To connect to a different server**, modify these constants or implement configuration file loading.

### UI Customization

All UI constants are centralized in `UIConstants.java`:

- **Colors**: Primary, secondary, accent colors
- **Fonts**: Font sizes and styles
- **Dimensions**: Window sizes, padding, spacing
- **RMI Services**: Service names for lookups

### Logging Configuration

Modify `logback.xml` to adjust:

- **Log levels**: Set to `debug`, `info`, `warn`, or `error`
- **Log output**: File location in `${LOG_HOME}` property
- **File rotation**: Max file size and retention policy
- **Package-specific levels**: Different logging for different packages

## Usage

### Running the Application

1. Ensure the RMI registry is running on `localhost:5000`:
   ```bash
   rmiregistry 5000
   ```

2. Start the application:
   ```bash
   java -jar target/ImportTaxClient-jar-with-dependencies.jar
   ```

3. **Login Screen**
   - Enter username and password
   - Click "LOGIN" or press Enter
   - Or click "EXIT" to close (Escape key also works)

4. **Dashboard Screen**
   - View system overview and metrics
   - Navigate using sidebar menu
   - Click "Logout" to return to login screen
   - Live clock updates in the header

### Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Enter | Login / Submit |
| Escape | Exit application / Cancel |
| Tab | Navigate between fields |

## Project Packages

### `com.importtax.client`
- **AppLauncher**: Application entry point, theme setup, main window launch

### `com.importtax.client.ui`
- **LoginFrame**: Professional login interface with validation
- **DashboardFrame**: Main dashboard with navigation and metrics

### `com.importtax.client.rmi`
- **RmiConnection**: RMI service lookup and connection management

### `com.importtax.client.util`
- **UIConstants**: Centralized UI configuration
- **RoundedPanel**: Custom JPanel with rounded corners
- **RoundedButton**: Custom JButton with modern styling

## Future Implementation Tasks

### Phase 2 - CRUD Operations
- [ ] Users Management Screen
- [ ] Import Items Management
- [ ] Tax Configuration Screen
- [ ] Invoice Management
- [ ] Payment Processing

### Phase 3 - Advanced Features
- [ ] Reports & Analytics
- [ ] Notification System
- [ ] Search & Filter functionality
- [ ] Data export (CSV, PDF)
- [ ] User preferences/settings

### Phase 4 - RMI Integration
- [ ] Implement actual RMI calls in LoginFrame
- [ ] Create remote service interfaces
- [ ] Error handling for RMI failures
- [ ] Implement session management

### Phase 5 - Security
- [ ] Password encryption
- [ ] SSL/TLS for RMI communication
- [ ] Token-based authentication
- [ ] Role-based access control (RBAC)

## Error Handling

### Logging Best Practices

The application uses SLF4J with Logback for comprehensive logging:

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

logger.info("Information message");
logger.warn("Warning message");
logger.error("Error message", exception);
logger.debug("Debug message");
```

### Common Issues

| Issue | Solution |
|-------|----------|
| "RMI registry not accessible" | Ensure RMI registry is running on port 5000 |
| "FlatLaf theme not applied" | Check that FlatLaf dependency is properly included |
| "MigLayout not recognized" | Verify MigLayout JAR is in classpath |
| "Logging not working" | Check logback.xml is in classpath (`src/main/resources/`) |

## Development Guidelines

### Code Style
- Follow Java 17 conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc for public methods
- Keep methods small and focused
- Implement proper exception handling

### UI Development
- Use constants from `UIConstants` class
- Leverage `RoundedPanel` and `RoundedButton` for consistency
- Apply MigLayout for responsive designs
- Test on different window sizes
- Ensure keyboard navigation works

### RMI Integration
- Always use try-catch for RemoteException
- Log all RMI operations
- Implement connection validation
- Handle network timeouts gracefully

## Building Executable JAR

The project includes Maven assembly plugin for creating a fat JAR:

```bash
mvn clean package -DskipTests
```

This generates:
- `target/ImportTaxClient-jar-with-dependencies.jar` - Executable JAR with all dependencies

Run it with:
```bash
java -jar target/ImportTaxClient-jar-with-dependencies.jar
```

## Performance Considerations

- **UI Threading**: All UI operations use SwingUtilities.invokeLater()
- **RMI Connection**: Singleton pattern for registry connection
- **Logging**: Asynchronous appenders reduce I/O blocking
- **Resource Management**: Proper disposal of frames and resources
- **Memory**: Live clock runs as daemon thread to prevent memory leaks

## Troubleshooting

### Compilation Issues
```bash
# Check Java version
java -version

# Should be 17+. If not, update your JAVA_HOME environment variable
```

### Dependency Issues
```bash
# Clear Maven cache and rebuild
mvn clean dependency:purge-local-repository package
```

### UI Issues
- Ensure display/graphics drivers are up to date
- Check screen resolution (tested at 1920x1080)
- Disable hardware acceleration if experiencing rendering issues

## License

**Import Tax Management System** - Copyright © 2026
Developed by Import Tax System Development Team

## Support & Documentation

For additional information:
- Check inline JavaDoc in source files
- Review logback.xml for logging configuration
- Examine UIConstants.java for UI customization options
- Refer to FlatLaf documentation: https://www.formdev.com/flatlaf/
- Refer to MigLayout documentation: http://www.miglayout.com/

## Version History

### v1.0.0 (2026-05-13)
- ✨ Initial release
- 🎨 Professional UI foundation with FlatLaf
- 🔐 Login interface with validation
- 📊 Dashboard with navigation and metrics
- 🌐 RMI connection utilities
- 📝 Comprehensive logging with Logback
- 📚 Full JavaDoc documentation

---

**Status**: Foundation Complete - Ready for CRUD Implementation
