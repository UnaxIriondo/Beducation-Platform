<script setup>        
import {ref} from 'vue'
let email = ref('email')
let password = ref('password')
</script>
<template>
    <label for="email" id="email">Email:</label>
    <input type="email" id="email" name="email" v-model="email">
    <label for="password" id="password">Password:</label>
    <input type="password" id="password" name="password" v-model="password">
</template>
<style scoped></style>