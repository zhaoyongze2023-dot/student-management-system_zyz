const WebSocket = require('ws');

const token = process.argv[2] || 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ6eXpfMTIzIiwiaWF0IjoxNzY1NTY2ODMyLCJleHAiOjE3NjYxNzE2MzJ9.wvjcDQM8RkvT1fIWkwacq0hDm0wTE8paLNlksMDXnrRPehVhna_bhpTybQ4yDK7dNvGZ5dz_IlF7zAc72JdAVQ';
const url = `ws://localhost:8080/api/ws/notification?token=${token}`;

console.log(`Connecting to WebSocket: ${url}`);

const ws = new WebSocket(url);

ws.on('open', () => {
    console.log('✅ WebSocket Connected!');
    console.log('Authentication successful - JWT token from URL parameter works!');
    
    // Send a test message
    ws.send(JSON.stringify({
        type: 'test',
        message: 'Hello WebSocket Server'
    }));
    
    // Close connection after 2 seconds
    setTimeout(() => {
        ws.close();
    }, 2000);
});

ws.on('message', (data) => {
    console.log(`📨 Received message:`, data.toString());
});

ws.on('error', (error) => {
    console.error('❌ WebSocket Error:', error.message);
    process.exit(1);
});

ws.on('close', () => {
    console.log('🔌 WebSocket Connection Closed');
    process.exit(0);
});
