import React, { useEffect, useState } from 'react';
import Collapse from '@mui/material/Collapse';
import axios from 'axios';
import {
  Container, Typography, Paper,
  Button, Box, TextField, Select, MenuItem, FormControl,
  InputLabel, OutlinedInput, Chip
} from '@mui/material';

export default function ProductList() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [editedProduct, setEditedProduct] = useState({ name: '', price: '', categories: [] });

  const [newName, setNewName] = useState('');
  const [newPrice, setNewPrice] = useState('');
  const [newCategories, setNewCategories] = useState([]);
  const [showForm, setShowForm] = useState(false);

  const fetchProducts = () => {
    axios.get('/api/products')
      .then(res => setProducts(res.data))
      .catch(err => console.error(err));
  };

  const fetchCategories = () => {
    axios.get('/api/categories')
      .then(res => setCategories(res.data))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Удалить продукт?')) return;
    try {
      await axios.delete(`/api/products/${id}`);
      fetchProducts();
    } catch (error) {
      console.error('Ошибка при удалении:', error);
      alert('❌ Не удалось удалить продукт');
    }
  };

  const startEditing = (product) => {
    setEditingId(product.id);
    setEditedProduct({
      name: product.name,
      price: product.price,
      categories: product.categories.map(c => c.id)
    });
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditedProduct({ name: '', price: '', categories: [] });
  };

  const saveChanges = async (id) => {
    try {
      await axios.put(`/api/products/${id}`, {
        name: editedProduct.name,
        price: parseFloat(editedProduct.price),
        categories: editedProduct.categories.map(id => ({ id }))
      });
      fetchProducts();
      cancelEditing();
    } catch (error) {
      console.error('Ошибка при обновлении:', error);
      alert('❌ Не удалось обновить продукт');
    }
  };

  const handleAddProduct = async () => {
    try {
      await axios.post(`/api/products`, {
        name: newName,
        price: parseFloat(newPrice),
        categories: newCategories.map(id => ({ id }))
      });
      setNewName('');
      setNewPrice('');
      setNewCategories([]);
      fetchProducts();
    } catch (error) {
      console.error('Ошибка при добавлении:', error);
      alert('❌ Не удалось добавить продукт');
    }
  };

  return (
    <Container>
      <Typography variant="h4" gutterBottom>Товары</Typography>
      
      <Box display="flex" justifyContent="flex-end" mb={3}>
        <Button
          variant="outlined"
          onClick={() => setShowForm(!showForm)}
        >
          {showForm ? '❌ Скрыть форму' : '➕ Добавить продукт'}
        </Button>
      </Box>

      <Collapse in={showForm}>
        <Paper className="paper-card" style={{ padding: 16, marginBottom: 24}}>
          <Typography variant="h6">➕ Добавить продукт</Typography>
          <TextField
            label="Название"
            value={newName}
            onChange={e => setNewName(e.target.value)}
            fullWidth
            margin="dense"
          />
          <TextField
            label="Цена"
            type="number"
            value={newPrice}
            onChange={e => setNewPrice(e.target.value)}
            fullWidth
            margin="dense"
          />
          <FormControl fullWidth margin="dense">
            <InputLabel>Категории</InputLabel>
            <Select
              multiple
              value={newCategories}
              onChange={e => setNewCategories(e.target.value)}
              input={<OutlinedInput label="Категории" />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((id) => {
                    const cat = categories.find(c => c.id === id);
                    return <Chip key={id} label={cat?.name || id} />;
                  })}
                </Box>
              )}
            >
              {categories.map(cat => (
                <MenuItem key={cat.id} value={cat.id}>
                  {cat.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Box mt={2}>
            <Button variant="contained" onClick={handleAddProduct}>Сохранить</Button>
          </Box>
        </Paper>
      </Collapse>

      {products.map(p => (
        <Paper
        className="paper-card"
        key={p.id}
      >
        {editingId === p.id ? (
          <>
            <TextField
              label="Название"
              value={editedProduct.name}
              onChange={e => setEditedProduct({ ...editedProduct, name: e.target.value })}
              fullWidth
              margin="normal"
              variant="outlined"
              size="small"
            />
            <TextField
              label="Цена"
              type="number"
              value={editedProduct.price}
              onChange={e => setEditedProduct({ ...editedProduct, price: e.target.value })}
              fullWidth
              margin="normal"
              variant="outlined"
              size="small"
            />
            <FormControl fullWidth margin="normal" size="small">
              <InputLabel>Категории</InputLabel>
              <Select
                multiple
                value={editedProduct.categories}
                onChange={e => setEditedProduct({ ...editedProduct, categories: e.target.value })}
                input={<OutlinedInput label="Категории" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((id) => {
                      const cat = categories.find(c => c.id === id);
                      return <Chip key={id} label={cat?.name || id} />;
                    })}
                  </Box>
                )}
              >
                {categories.map(cat => (
                  <MenuItem key={cat.id} value={cat.id}>
                    {cat.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Box mt={2} sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" onClick={() => saveChanges(p.id)}>💾 Сохранить</Button>
              <Button variant="text" onClick={cancelEditing}>Отмена</Button>
            </Box>
          </>
        ) : (
          <>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                {p.name} — 💲{p.price}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" onClick={() => startEditing(p)}>✏️ Редактировать</Button>
                <Button variant="outlined" color="error" onClick={() => handleDelete(p.id)}>🗑️ Удалить</Button>
              </Box>
            </Box>
            {p.categories && p.categories.length > 0 ? (
            <>
              <Typography variant="subtitle2" sx={{ color: '#6b7280', mb: 1 }}>Категории:</Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {p.categories.map((c) => (
                  <Chip
                    key={c.id}
                    label={c.name}
                    sx={{
                      flex: '0 0 10%',      
                      maxWidth: '30%',      
                      minWidth: '100px',    
                      boxSizing: 'border-box'
                    }}
                  />
                ))}
              </Box>
            </>
          ) : (
            <Typography variant="subtitle2" sx={{ color: '#6b7280', mb: 1, fontStyle: 'italic' }}>
              Категории отсутствуют
            </Typography>
          )}
          </>
        )}
      </Paper>
      ))}
    </Container>
  );
}