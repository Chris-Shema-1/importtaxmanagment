# Developer Quick Reference Guide

## Common Code Snippets

### Creating a New UI Frame

```java
package com.importtax.client.ui;

import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.importtax.client.util.UIConstants;

public class MyNewFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(MyNewFrame.class);

    public MyNewFrame() {
        logger.info("Initializing MyNewFrame");
        initializeFrame();
        createComponents();
        setupLayout();
    }

    private void initializeFrame() {
        setTitle(UIConstants.APP_TITLE + " - My Feature");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setBackground(UIConstants.BACKGROUND_COLOR);
    }

    private void createComponents() {
        // Create UI components here
    }

    private void setupLayout() {
        // Setup layout here
    }
}
```

### Using Rounded Panel

```java
import com.importtax.client.util.RoundedPanel;
import com.importtax.client.util.UIConstants;

// Create a rounded panel with border
RoundedPanel panel = new RoundedPanel(
    UIConstants.BORDER_RADIUS,           // arc width
    UIConstants.BORDER_RADIUS,           // arc height
    UIConstants.PANEL_COLOR,             // background color
    UIConstants.BORDER_COLOR,            // border color
    1                                    // border width
);
```

### Using Rounded Button

```java
import com.importtax.client.util.RoundedButton;
import com.importtax.client.util.UIConstants;

// Create a button with custom colors
RoundedButton button = new RoundedButton("Click Me");
button.setStateColors(
    UIConstants.PRIMARY_COLOR,           // default color
    UIConstants.PRIMARY_LIGHT,           // hover color
    UIConstants.PRIMARY_DARK             // pressed color
);
button.addActionListener(e -> handleButtonClick());
```

### RMI Service Lookup

```java
import com.importtax.client.rmi.RmiConnection;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

try {
    // Initialize connection (only needed once)
    RmiConnection.initialize();
    
    // Lookup a service
    MyRemoteService service = RmiConnection.lookup("MyService");
    
    // Use the service
    String result = service.someRemoteMethod("param");
    
} catch (RemoteException e) {
    logger.error("RMI communication error", e);
    showError("Connection error: " + e.getMessage());
} catch (NotBoundException e) {
    logger.error("Service not found", e);
    showError("Service unavailable");
}
```

### Logging Best Practices

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

// Debug - detailed diagnostic information
logger.debug("Processing item: {}", itemId);

// Info - confirmation that things are working
logger.info("User {} logged in successfully", username);

// Warn - something unexpected but recoverable
logger.warn("Retry attempt {} for service {}", retryCount, serviceName);

// Error - error condition with exception
logger.error("Failed to process transaction", exception);
```

### MigLayout Examples

```java
import net.miginfocom.swing.MigLayout;
import javax.swing.JPanel;

// Simple 2-column layout
JPanel panel = new JPanel(new MigLayout("insets 10, gap 10"));
panel.add(label1, "wrap");         // wrap to next row
panel.add(field1, "grow, wrap");   // grow with available space

// Grid layout with alignment
JPanel grid = new JPanel(new MigLayout("fillx, filly", "[grow]", "[grow]"));
grid.add(component1, "grow, span 2"); // span 2 columns

// Sidebar layout
JPanel sidebar = new JPanel(new MigLayout("", "", "[grow]"));
sidebar.add(navButton1, "wrap");
sidebar.add(navButton2, "wrap");
sidebar.add(Box.createVerticalGlue());
sidebar.add(logoutButton);
```

### Form Validation

```java
private boolean validateLoginForm() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();
    
    if (username.isEmpty()) {
        showError("Username cannot be empty");
        usernameField.requestFocus();
        return false;
    }
    
    if (password.isEmpty()) {
        showError("Password cannot be empty");
        passwordField.requestFocus();
        return false;
    }
    
    if (username.length() < 3) {
        showError("Username must be at least 3 characters");
        return false;
    }
    
    return true;
}
```

### Dialog Messages

```java
// Info dialog
JOptionPane.showMessageDialog(this, "Operation completed successfully",
    "Success", JOptionPane.INFORMATION_MESSAGE);

// Error dialog
JOptionPane.showMessageDialog(this, "An error occurred: " + errorMsg,
    "Error", JOptionPane.ERROR_MESSAGE);

// Confirmation dialog
int confirmed = JOptionPane.showConfirmDialog(this,
    "Are you sure you want to continue?",
    "Confirm Action", JOptionPane.YES_NO_OPTION);

if (confirmed == JOptionPane.YES_OPTION) {
    // User clicked Yes
}

// Input dialog
String input = JOptionPane.showInputDialog(this, "Enter value:");
if (input != null) {
    // User entered something
}
```

### Background Task Execution

```java
// Execute long-running operation on separate thread
new Thread(() -> {
    try {
        statusLabel.setText("Processing...");
        
        // Long operation
        result = doLongOperation();
        
        // Update UI on EDT
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Done!");
            resultLabel.setText(result);
        });
    } catch (Exception e) {
        logger.error("Background operation failed", e);
        SwingUtilities.invokeLater(() -> 
            statusLabel.setText("Error: " + e.getMessage())
        );
    }
}).start();
```

### Color Utilities

```java
import com.importtax.client.util.UIConstants;
import java.awt.Color;

// Create semi-transparent color
Color transparent = UIConstants.withAlpha(UIConstants.PRIMARY_COLOR, 128);

// Blend two colors
Color blended = UIConstants.blend(
    UIConstants.PRIMARY_COLOR,
    UIConstants.ACCENT_COLOR,
    0.5f  // 50% blend
);

// Darker/lighter colors
Color darker = UIConstants.PRIMARY_COLOR.darker();
Color lighter = new Color(
    Math.min(255, UIConstants.PRIMARY_COLOR.getRed() + 50),
    Math.min(255, UIConstants.PRIMARY_COLOR.getGreen() + 50),
    Math.min(255, UIConstants.PRIMARY_COLOR.getBlue() + 50)
);
```

### Keyboard Shortcuts

```java
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

InputMap inputMap = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
ActionMap actionMap = getRootPane().getActionMap();

// Bind Enter key
inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
actionMap.put("enter", new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent e) {
        handleEnter();
    }
});

// Bind Escape key
inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
actionMap.put("escape", new AbstractAction() {
    @Override
    public void actionPerformed(ActionEvent e) {
        handleEscape();
    }
});
```

### Focus Effects for Text Fields

```java
import javax.swing.JTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.border.EmptyBorder;

JTextField field = new JTextField();

field.addFocusListener(new FocusAdapter() {
    @Override
    public void focusGained(FocusEvent e) {
        field.setBackground(UIConstants.PANEL_COLOR.brighter());
        field.setBorder(BorderFactory.createLineBorder(
            UIConstants.PRIMARY_COLOR, 2));
    }

    @Override
    public void focusLost(FocusEvent e) {
        field.setBackground(UIConstants.PANEL_COLOR);
        field.setBorder(new EmptyBorder(10, 15, 10, 15));
    }
});
```

## Common UI Patterns

### Header Section
```java
private JPanel createHeader() {
    RoundedPanel header = new RoundedPanel(0, 0, UIConstants.PANEL_COLOR.brighter());
    header.setPreferredSize(new Dimension(0, UIConstants.HEADER_HEIGHT));
    header.setLayout(new MigLayout("insets 10 20 10 20, fillx"));
    
    JLabel title = new JLabel("Title");
    title.setFont(UIConstants.FONT_HEADER);
    title.setForeground(UIConstants.TEXT_COLOR);
    header.add(title, "grow");
    
    return header;
}
```

### Card Panel
```java
private JPanel createCard(String title, String value, Color accentColor) {
    RoundedPanel card = new RoundedPanel(UIConstants.BORDER_RADIUS,
        UIConstants.BORDER_RADIUS, UIConstants.PANEL_COLOR,
        accentColor, 2);
    card.setLayout(new MigLayout("insets 15, center"));
    
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(UIConstants.FONT_SUBHEADER);
    titleLabel.setForeground(UIConstants.TEXT_SECONDARY);
    card.add(titleLabel, "wrap");
    
    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    valueLabel.setForeground(accentColor);
    card.add(valueLabel);
    
    return card;
}
```

### Navigation Sidebar
```java
private JPanel createSidebar() {
    JPanel sidebar = new JPanel();
    sidebar.setBackground(UIConstants.PANEL_COLOR);
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
    
    String[] items = {"Item 1", "Item 2", "Item 3"};
    for (String item : items) {
        RoundedButton btn = new RoundedButton(item);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.addActionListener(e -> handleNavigation(item));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(10));
    }
    
    sidebar.add(Box.createVerticalGlue());
    
    RoundedButton logoutBtn = new RoundedButton("Logout");
    logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    logoutBtn.addActionListener(e -> handleLogout());
    sidebar.add(logoutBtn);
    
    return sidebar;
}
```

## Configuration Changes

### Update RMI Connection
```
File: src/main/java/com/importtax/client/rmi/RmiConnection.java

Line 20-22:
private static final String RMI_HOST = "your-server-host";
private static final int RMI_PORT = your_port_number;
private static final String RMI_URL = "rmi://your-server:port/";
```

### Add New Service Name
```
File: src/main/java/com/importtax/client/util/UIConstants.java

Add to RMI Configuration section:
public static final String RMI_SERVICE_NEW = "NewService";
```

### Change Application Colors
```
File: src/main/java/com/importtax/client/util/UIConstants.java

Update Color Palette section:
public static final Color PRIMARY_COLOR = new Color(R, G, B);
```

## Testing Checklist

- [ ] All buttons respond to clicks
- [ ] Keyboard shortcuts work (Enter, Escape, Tab)
- [ ] Hover effects display correctly
- [ ] Forms validate input
- [ ] Error messages appear
- [ ] Navigation items work
- [ ] Logout returns to login
- [ ] Clock updates in real-time
- [ ] Application resizes properly
- [ ] Logging writes to file

## Performance Tips

1. Use `lazy initialization` for expensive components
2. Execute long operations on separate threads
3. Use `SwingWorker` for background tasks
4. Cache frequently accessed resources
5. Dispose of resources properly
6. Use `CardLayout` for switching panels efficiently
7. Enable `double buffering` in Swing components
8. Avoid blocking the Event Dispatch Thread

## Common Mistakes to Avoid

❌ **Don't**
- Access Swing components from non-EDT threads
- Create endless loops in EDT
- Forget to dispose frames
- Hardcode values instead of using constants
- Ignore exceptions silently
- Block the UI thread with long operations
- Create infinite loops in listeners

✅ **Do**
- Use `SwingUtilities.invokeLater()` for UI updates
- Execute long operations on separate threads
- Always log errors and important events
- Use `UIConstants` for all configuration
- Handle exceptions properly
- Test responsive behavior
- Use proper naming conventions

## Quick Debugging Tips

```java
// Print component hierarchy
printTree(getRootPane(), 0);

private void printTree(Component c, int depth) {
    System.out.println("  ".repeat(depth) + c.getClass().getSimpleName());
    if (c instanceof Container) {
        for (Component child : ((Container) c).getComponents()) {
            printTree(child, depth + 1);
        }
    }
}

// Check component sizes
System.out.println("Size: " + component.getSize());
System.out.println("Preferred: " + component.getPreferredSize());
System.out.println("Location: " + component.getLocation());

// Monitor layout
System.setProperty("swing.layout.debug", "true");
```

---

**Last Updated**: 2026-05-13
**Version**: 1.0.0
