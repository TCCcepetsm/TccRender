const fetch = require('node-fetch');
const cron = require('node-cron');

// Executa a cada 10 minutos
cron.schedule('*/10 * * * *', async () => {
    try {
        await fetch('https://recorder-backend-7r85.onrender.com/actuator/health');
        console.log('Keep-alive executado:', new Date().toISOString());
    } catch (error) {
        console.error('Erro no keep-alive:', error);
    }
});

console.log('Serviço keep-alive iniciado...');