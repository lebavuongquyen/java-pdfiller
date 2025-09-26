#!/bin/bash

echo "🚀 Creating distribution package for Ubuntu..."

# Go to project root from script location
cd "$(dirname "$0")/.."

echo "Running Maven build..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "❌ Maven build failed!"
    exit 1
fi

echo "Creating dist directory..."
DIST_DIR="dist"
if [ -d "$DIST_DIR" ]; then
    rm -rf "$DIST_DIR"
fi
mkdir "$DIST_DIR"

echo "Copying artifacts..."
cp "target/pdffiller.jar" "$DIST_DIR/"
cp "target/pdffiller-jar-with-dependencies.jar" "$DIST_DIR/"
cp "shell/pdffiller.dist.bat" "$DIST_DIR/pdffiller.bat"
cp "shell/pdffiller.dist.sh" "$DIST_DIR/pdffiller.sh"
cp "README.md" "$DIST_DIR/"
chmod +x "$DIST_DIR/pdffiller.sh"

echo "✅ Distribution package created in 'dist' folder."
echo "To run the application, navigate to the 'dist' folder and use:"
echo "./pdffiller.sh start"
echo "or"
echo "./pdffiller.sh fill ..."