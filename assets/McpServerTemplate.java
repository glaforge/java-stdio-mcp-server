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
