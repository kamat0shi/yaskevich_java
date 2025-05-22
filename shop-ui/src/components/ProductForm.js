import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  TextField, Button, Typography, Box, Paper,
  InputLabel, Select, MenuItem, FormControl, OutlinedInput, Chip
} from '@mui/material';

const ProductForm = () => {
  const [name, setName] = useState('');
  const [price, setPrice] = useState('');
  const [categories, setCategories] = useState([]);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [response, setResponse] = useState(null);

  useEffect(() => {
    axios.get('/api/categories')
      .then(res => setCategories(res.data))
      .catch(err => console.error('Ошибка при загрузке категорий:', err));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('/api/products', {
        name,
        price: parseFloat(price),
        categories: selectedCategories.map(id => ({ id }))
      });
      setResponse(res.data);
    } catch (err) {
      console.error('Ошибка при отправке запроса:', err);
      setResponse({ error: err.message });
    }
  };

  return (
    <Paper elevation={3} sx={{ padding: 4, maxWidth: 600, margin: 'auto', marginTop: 4 }}>
      <Typography variant="h6" gutterBottom>Добавить продукт</Typography>
      <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <TextField label="Название" value={name} onChange={e => setName(e.target.value)} required />
        <TextField label="Цена" type="number" value={price} onChange={e => setPrice(e.target.value)} required />

        <FormControl fullWidth>
          <InputLabel>Категории</InputLabel>
          <Select
            multiple
            value={selectedCategories}
            onChange={e => setSelectedCategories(e.target.value)}
            input={<OutlinedInput label="Категории" />}
            renderValue={(selected) =>
              selected.map(id => {
                const cat = categories.find(c => c.id === id);
                return cat ? cat.name : id;
              }).join(', ')
            }
          >
            {categories.map(cat => (
              <MenuItem key={cat.id} value={cat.id}>
                {cat.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Button type="submit" variant="contained">Сохранить</Button>
      </Box>
      {response && (
        <Box mt={2}>
          <Typography>Ответ сервера:</Typography>
          <pre>{JSON.stringify(response, null, 2)}</pre>
        </Box>
      )}
    </Paper>
  );
};

export default ProductForm;