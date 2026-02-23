---
name: jbang-mcp-server
description: Scaffolds and installs zero boilerplate Java-based MCP STDIO servers using JBang and LangChain4j for Gemini CLI. Use this to quickly bootstrap an MCP server from scratch.
license: Apache-2.0
compatibility: Requires a modern version of Java, and the installation of the JBang command-line tool
metadata:
    author: Guillaume Laforge
    version: "0.1"
---

# JBang LangChain4j MCP Server Creator

This skill helps quickly scaffold a new Java-based MCP STDIO server using JBang and LangChain4j, and installs it into Gemini CLI's `settings.json`.

## Process

1.  **Ask User for Details:**
    *   Desired file name (e.g., `McpToolServer.java`) and path to save it.
    *   The name of the server to register in `~/.gemini/settings.json` (e.g., `java-calc`).
    *   (Optional) High-level description of the tools they want to add initially.

2.  **Scaffold the Server:**
    *   Read the template file located at `assets/McpServerTemplate.java` using the `read_file` tool. Note that the path to `assets/McpServerTemplate.java` needs to be resolved relative to the skill directory or read from the skill's bundled assets. As an alternative if the absolute path is unknown, directly write out the template contents described below.
    *   Replace `{SERVER_CLASS_NAME}` with the base name of the requested Java file (e.g., `McpToolServer` if file is `McpToolServer.java`).
    *   Replace `{TOOL_CLASS_NAME}` with a related name (e.g., `MyTools`).
    *   (Optional) Modify the `@Tool` annotated methods to reflect the user's requirements.
    *   Write the finalized content to the user's requested path using the `write_file` tool.
    *   Make the file executable using `chmod +x <path_to_file>` via `run_shell_command`.

3.  **Verify the Server:**
    *   Run `jbang build <path_to_file>` using the `run_shell_command` tool to check for any compilation errors.
    *   If there are compilation errors (e.g., unclosed string literals due to unescaped newlines), use the `replace` tool to fix them.
    *   Repeat the compilation check until successful.

4.  **Install the Server in Gemini CLI:**
    *   Read `~/.gemini/settings.json`.
    *   Use the `replace` tool or jq via `run_shell_command` to inject a new entry under `mcpServers`.
    *   The new entry should look like this:
        ```json
        "{server_name}": {
          "command": "jbang",
          "args": [
            "run",
            "--quiet",
            "{absolute_path_to_java_file}"
          ]
        }
        ```
    *   Inform the user that the server has been created and configured, and remind them that Gemini CLI automatically reloads configurations.

## Template Backup
If `assets/McpServerTemplate.java` cannot be read, use this template:
```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS dev.langchain4j:langchain4j-core:1.11.0
//DEPS dev.langchain4j:langchain4j-community-mcp-server:1.11.0-beta19
//DEPS org.slf4j:slf4j-simple:2.0.17
//JAVA 21

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.mcp.server.McpServer;
import dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class {SERVER_CLASS_NAME} {

    static {
        // Configure SLF4J Simple Logger to write to System.err
        // This is crucial for MCP servers over STDIO to avoid polluting stdout
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.err");
    }

    private static final Logger log = LoggerFactory.getLogger({SERVER_CLASS_NAME}.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting LangChain4j MCP Server...");

        // Instantiate tools
        {TOOL_CLASS_NAME} tools = new {TOOL_CLASS_NAME}();

        // Create Server
        McpServer server = new McpServer(List.of(tools));

        // Start Transport
        StdioMcpServerTransport transport = new StdioMcpServerTransport(server);

        log.info("MCP Server started successfully on STDIO.");

        // Keep Alive
        new CountDownLatch(1).await();
    }

    // --- Tool Definition ---
    public static class {TOOL_CLASS_NAME} {

        @Tool("Description of your tool")
        public String sampleTool(String input) {
            log.info("Called sampleTool with {}", input);
            return "Processed: " + input;
        }
    }
}
```

## Key Rules
*   **Logging:** JBang STDIO servers MUST write all logs to `System.err` to avoid polluting the JSON-RPC standard output stream. This is already handled in the template via `System.setProperty("org.slf4j.simpleLogger.logFile", "System.err");` but ensure this is maintained if modifying the file structure.
*   **Dependencies:** The template relies on LangChain4j and slf4j-simple. Do not remove the `//DEPS` directives at the top of the template.
