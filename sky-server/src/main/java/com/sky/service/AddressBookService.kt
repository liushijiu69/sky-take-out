package com.sky.service

import com.sky.entity.AddressBook

interface AddressBookService {
    fun list(addressBook: AddressBook): List<AddressBook>
    fun save(addressBook: AddressBook)
    fun getById(id: Long): AddressBook
    fun update(addressBook: AddressBook)
    fun setDefault(addressBook: AddressBook)
    fun deleteById(id: Long)
}
