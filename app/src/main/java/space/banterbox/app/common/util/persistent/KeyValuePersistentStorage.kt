package space.banterbox.app.common.util.persistent

import space.banterbox.app.common.util.persistent.KeyValueDataSet

interface KeyValuePersistentStorage {
    fun writeDateSet(dataSet: KeyValueDataSet, removes: Collection<String>)
    fun getDataSet(): KeyValueDataSet
}