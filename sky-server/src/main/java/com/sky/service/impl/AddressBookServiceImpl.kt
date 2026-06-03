package com.sky.service.impl

import com.sky.context.BaseContext
import com.sky.entity.AddressBook
import com.sky.mapper.AddressBookMapper
import com.sky.service.AddressBookService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddressBookServiceImpl(
    private val addressBookMapper: AddressBookMapper
) : AddressBookService {

    override fun list(addressBook: AddressBook): List<AddressBook> {
        return addressBookMapper.list(addressBook)
    }

    override fun save(addressBook: AddressBook) {
        addressBook.userId = BaseContext.getCurrentId()
        addressBook.isDefault = 0
        addressBookMapper.insert(addressBook)
    }

    override fun getById(id: Long): AddressBook {
        return addressBookMapper.getById(id)
    }

    override fun update(addressBook: AddressBook) {
        addressBookMapper.update(addressBook)
    }

    @Transactional
    override fun setDefault(addressBook: AddressBook) {
        // 将当前用户的所有地址修改为非默认
        addressBook.isDefault = 0
        addressBook.userId = BaseContext.getCurrentId()
        addressBookMapper.updateIsDefaultByUserId(addressBook)
        // 将当前地址改为默认
        addressBook.isDefault = 1
        addressBookMapper.update(addressBook)
    }

    override fun deleteById(id: Long) {
        addressBookMapper.deleteById(id)
    }
}
