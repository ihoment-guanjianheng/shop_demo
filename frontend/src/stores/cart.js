import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0))

  function save() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  function addItem(item) {
    const existing = items.value.find(i => i.skuId === item.skuId)
    if (existing) {
      existing.quantity += item.quantity
    } else {
      items.value.push(item)
    }
    save()
  }

  function removeItem(skuId) {
    items.value = items.value.filter(i => i.skuId !== skuId)
    save()
  }

  function updateQuantity(skuId, quantity) {
    const item = items.value.find(i => i.skuId === skuId)
    if (item) {
      item.quantity = quantity
      save()
    }
  }

  function clear() {
    items.value = []
    save()
  }

  return { items, totalCount, totalAmount, addItem, removeItem, updateQuantity, clear }
})