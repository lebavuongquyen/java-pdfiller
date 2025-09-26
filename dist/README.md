# PDF Filler Application

## 1. Overview

This application allows users to fill PDF form fields programmatically using data from a JSON object. It is a Spring Boot application that can be operated in three ways:

1.  **Web Server**: Runs as a server with a REST API for filling PDFs.
2.  **Command-Line Interface (CLI)**: A command-line tool for direct PDF manipulation.
3.  **Web UI**: A simple, built-in web interface for easy testing and documentation browsing.

---

## 2. Prerequisites

- **Java**: Version 21 or higher.
- **Maven**: Required only for building the project from the source code.

---

## 3. Installation & Building

To create a portable, distributable version of the application, run the appropriate installation script from the project root directory.

**On Windows:**
```bash
bin\install.bat
```

**On Linux/macOS:**
```bash
bash bin/install.sh
```

This script will perform the following actions:
1.  Build the application using Maven.
2.  Create a `dist` directory in the project root.
3.  Copy the necessary JAR files, runner scripts, and this README into the `dist` directory.

The `dist` folder is now a self-contained, portable application that you can zip and move to any machine with Java installed.

---

## 4. Usage

All commands should be run from within the `dist` directory.

### 4.1. Starting the Web Server

This starts the HTTP server, including the REST API and the web UI.

**On Windows:**
```bash
pdffiller.bat start [port]
```

**On Linux/macOS:**
```bash
./pdffiller.sh start [port]
```

- `[port]` is optional. If not provided, the server will start on the default port (9001).

Once started, you can access:
- **Test UI**: `http://localhost:[port]/`
- **Documentation**: `http://localhost:[port]/documents`

### 4.2. Using the Command-Line Interface (CLI)

This allows you to fill a PDF directly from the command line.

**On Windows:**
```bash
pdffiller.bat fill [options]
```

**On Linux/macOS:**
```bash
./pdffiller.sh fill [options]
```

- `[options]` are the specific arguments for the CLI tool. Use the `--help` flag for more details on available options.

---

## 5. API Documentation

### Endpoint: `POST /api/pdf/fill`

Fills a PDF form based on the provided data.

- **Content-Type**: `multipart/form-data`

#### Parameters

| Name           | Type    | Required      | Description                                                                                                                            |
|----------------|---------|---------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `source`       | File    | Conditional   | The source PDF template file. Required if `sourceBase64` is not provided.                                                              |
| `sourceBase64` | String  | Conditional   | A Base64-encoded string of the source PDF. Required if `source` is not provided.                                                       |
| `data`         | String  | **Yes**       | A JSON string where keys match PDF field names. For images, the value must be a raw Base64 string of the image.                        |
| `flatten`      | Boolean | No            | If `true`, the form fields will be flattened (made non-editable) in the output PDF. Defaults to `false`.                               |
| `filename`     | String  | No            | If provided, the API returns the file as a download (`application/pdf`). If omitted, the API returns a Base64 string of the result. |

#### Example `curl` Request

```bash
curl -X POST http://localhost:9001/api/pdf/fill \
     -F "source=@template.pdf" \
     -F "data={\"name\":\"John Doe\",\"date\":\"2025-09-26\"}" \
     -F "filename=output.pdf" \
     -o output.pdf
```

---

## 6. NGINX Reverse Proxy Setup

To run this application behind a domain name (e.g., `pdffiller.yourdomain.com`), you can use NGINX as a reverse proxy. This allows you to handle SSL (HTTPS) and serve the application on standard ports 80/443.

Below are the steps and a template for setting up a virtual host on a typical Ubuntu server.

### Step 1: Create an NGINX Configuration File

Create a new configuration file for your site. Replace `yourdomain.com` with your actual domain.

```bash
sudo nano /etc/nginx/sites-available/pdffiller.yourdomain.com
```

### Step 2: Paste and Edit the Configuration

Paste the following template into the file. **You must edit the `server_name` and `proxy_pass` port** to match your setup.

```nginx
# /etc/nginx/sites-available/pdffiller.yourdomain.com

server {
    listen 80;
    listen [::]:80;

    # Change this to your domain name
    server_name pdffiller.yourdomain.com;

    # Optional: Redirect all HTTP traffic to HTTPS
    # For this to work, you must have an SSL certificate (see Step 5)
    # return 301 https://$host$request_uri;

    location / {
        proxy_set_header  Host $host;
        proxy_set_header  X-Real-IP $remote_addr;
        proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header  X-Forwarded-Proto $scheme;

        # Change this port to match the port your Java application is running on
        proxy_pass        http://localhost:9001;
    }
}
```

### Step 3: Enable the Site

Create a symbolic link from your new configuration file to the `sites-enabled` directory.

```bash
sudo ln -s /etc/nginx/sites-available/pdffiller.yourdomain.com /etc/nginx/sites-enabled/
```

### Step 4: Test and Reload NGINX

First, test your NGINX configuration to make sure there are no syntax errors.

```bash
sudo nginx -t
```

If the test is successful, reload NGINX to apply the changes.

```bash
sudo systemctl reload nginx
```

### Step 5 (Optional but Recommended): Enable HTTPS with Certbot

If you want to secure your site with a free SSL certificate from Let\'s Encrypt, you can use Certbot.

1.  Install Certbot: `sudo apt install certbot python3-certbot-nginx`
2.  Run Certbot and follow the on-screen instructions. It will automatically edit your NGINX configuration and set up SSL.

```bash
sudo certbot --nginx -d pdffiller.yourdomain.com
```

Your application should now be accessible via your domain name.

---

## 7. Building a Native Executable (Advanced)

You can compile this application into a standalone native executable (e.g., `.exe` on Windows) that does not require a Java Runtime Environment to be installed on the target machine. This is achieved using GraalVM Native Image.

**Note:** This process only compiles the main web server application, not the separate CLI tool.

### Step 1: Install GraalVM

You must install a GraalVM distribution for Java 21. You can find the official distributions and installation instructions at:

- **[GraalVM for JDK 21](https://www.graalvm.org/downloads/)**

After installing GraalVM, ensure that the `GRAALVM_HOME` environment variable points to the installation directory and that `$GRAALVM_HOME/bin` (or `%GRAALVM_HOME%\bin` on Windows) is in your `PATH`.

### Step 2: Install the Native Image Tool

Once GraalVM is installed, install the `native-image` component using the GraalVM Updater tool (`gu`):

```bash
gu install native-image
```

### Step 3: Run the Native Build

With GraalVM set as your Java environment, run the following Maven command from the project root directory. This will activate the `native` profile defined in `pom.xml`.

```bash
mvn -Pnative -DskipTests package
```

- The build process can take several minutes and may consume a significant amount of memory.
- The `-DskipTests` flag is recommended as native tests require additional configuration.

### Step 4: Find the Executable

Once the build is complete, you will find the native executable in the `target` directory.
- On Windows: `target\pdffiller.exe`
- On Linux/macOS: `target/pdffiller`

This single file is the standalone application. You can run it directly, and it will start the web server. For example, on Windows:

```bash
target\pdffiller.exe --server.port=9001
```
