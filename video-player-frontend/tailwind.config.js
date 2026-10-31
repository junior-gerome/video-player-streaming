/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}"
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          red: '#e11d48',
          dark: '#be123c',
          light: '#f43f5e'
        },
        surface: {
          950: '#09090b',
          900: '#111114',
          800: '#18181c',
          700: '#222228',
          600: '#2c2c34',
          500: '#3f3f4c'
        }
      },
      aspectRatio: {
        'video': '16 / 9'
      },
      fontFamily: {
        sans: ['"Inter"', '"Plus Jakarta Sans"', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif']
      }
    }
  },
  plugins: []
}
