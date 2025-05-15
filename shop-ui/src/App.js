// src/App.js
import React from 'react';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import ProductList from './components/ProductList';

const theme = createTheme({
  palette: {
    primary: {
      main: '#4f4f4f', // основной цвет для primary
      contrastText: '#fff', // цвет текста
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        outlinedPrimary: {
          '--variant-outlinedColor': '#4f4f4f',
          '--variant-outlinedBorder': '#4f4f4f',
          '--variant-outlinedBg': '#fff',
          '&:hover': {
            '--variant-outlinedColor': '#000000',
            '--variant-outlinedBorder': '#000000',
            '--variant-outlinedBg': '#f0f0f0',
          },
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <ProductList />
    </ThemeProvider>
  );
}

export default App;