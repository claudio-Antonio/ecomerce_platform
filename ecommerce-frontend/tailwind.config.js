/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      fontFamily: {
        // Fontes padrão de sistema comerciais (estilo Amazon Ember)
        display: ['system-ui', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
        body:    ['system-ui', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
      },
      colors: {
        amazon: {
          darkblue: '#131921',   // O azul escuro clássico da barra da amazon
          navbelw:  '#232f3e',   // O azul secundário usado em menus inferiores
          yellow:   '#ff9900',   // O laranja/amarelo do logo e botões principais
          gold:     '#febd69',   // O dourado de realce e botões secundários
          graybg:   '#eaeded',   // O cinza claro de fundo do site da amazon
        }
      }
    },
  },
  plugins: [],
}