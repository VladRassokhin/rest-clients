package com.github.goldin.rest.common

import java.lang.reflect.Field
import java.util.Date
import jet.List
import jet.Map
import kotlin.test.assertNotNull
import jet.Collection
import kotlin.nullable.toList
import kotlin.test.assertTrue
import java.math.BigDecimal
import com.google.common.collect.Sets
import jet.Set
import com.google.api.client.json.JsonToken


/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~
 * General purpose utilities.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

val OBJECT_CLASS  : Class<jet.Any>  = javaClass<jet.Any>()
val STRING_CLASS  : Class<String>  = javaClass<String>()
val INTEGER_CLASS : Class<jet.Int> = javaClass<jet.Int>()
val DATE_CLASS    : Class<Date>    = javaClass<Date>()
val BOOLEAN_CLASS : Class<Boolean> = javaClass<Boolean>()


/**
 * Updates object specified using map of its fields (name => value), converting them to an appropriate type.
 * Returns map of unrecognized fields (fields that don't appear to be object's fields).
 */
public fun updateObject( o : Any, map: Map<String, Any> ): Map<String, Any>
{
    val unrecognizedFields: MutableMap<String, Any> = hashMap()

   /**
    * Mapping of object fields: field name => field instance
    */
    val objectFields = o.javaClass.getFields()!!.convertToMap<Field, String, Field> {
        field -> Pair( field.getName()!! , field )
    }

    for ( e : Map.Entry<String, Any>? in map )
    {
        val fieldName           : String = e!!.key
        val fieldValue          : Any    = e!!.value
        val field               : Field? = objectFields.get( fieldName )
        val fieldValueConverted : Any?   = if ( fieldValue.javaClass != OBJECT_CLASS ) /* Json marker for null values */
                                                convertValue( fieldValue, field?.getType() ?: STRING_CLASS ) else
                                                null
        if ( fieldValueConverted != null )
        {
            if ( field == null )
            {   // The Object has no such field
                unrecognizedFields.put( fieldName, fieldValueConverted )
            }
            else
            {
                field.set( o, fieldValueConverted )
            }
        }
    }

    return unrecognizedFields
}


/**
 * Updates object specified using map of its fields (name => value), converting them to an appropriate type.
 * Returns object itself.
 */
public fun <T> updateObject( o : T, map: Map<String, Any>, ignoredFields : List<String>? = null ) : T
{
    val unrecognizedFieldsMap : MutableMap<String, Any> = hashMap()
    unrecognizedFieldsMap.putAll( updateObject( o, map ))

    if ( unrecognizedFieldsMap.isEmpty())
    {
        return o
    }

    val unrecognizedFields : MutableSet<String> = hashSet()
    unrecognizedFields.addAll( unrecognizedFieldsMap.keySet())
    if (( ignoredFields != null ) && ( ignoredFields.size > 0 ))
    {
        unrecognizedFields.removeAll( ignoredFields )
    }

    if ( unrecognizedFields.isEmpty())
    {
        return o
    }

    assertTrue( unrecognizedFieldsMap.isEmpty(),
                "Updating instance of class [${ o.javaClass.getName() }] using $map resulted " +
                "in unrecognized fields $unrecognizedFields" )
    return o
}


/**
 * Converts value to the type specified.
 */
public fun convertValue( value : Any, targetType: Class<out Any?> ): Any
{
    val valueStringifed : String     = if (( value is List<*> ) && ( value.size() == 1 )) value.get( 0 ).toString() else value.toString()
    val valueType       : Class<Any> = value.javaClass
    var valueConverted  : Any?

    try
    {
        if ( targetType == STRING_CLASS )
        {
            valueConverted = if ( valueType == STRING_CLASS ) value else valueStringifed
        }
        else if ( targetType == INTEGER_CLASS )
        {
            valueConverted = if ( valueType == INTEGER_CLASS ) value else Integer.valueOf( valueStringifed )
        }
        else if ( targetType == DATE_CLASS )
        {
            valueConverted = if ( valueType == DATE_CLASS    ) value else Date( valueStringifed.toLong())
        }
        else if ( targetType == BOOLEAN_CLASS )
        {   // http://youtrack.jetbrains.com/issue/KT-2318
            valueConverted = if ( valueType.getName() == "java.lang.Boolean" ) value else valueStringifed.toBoolean()
        }
        else
        {
            valueConverted = null
        }
    }
    catch( t: Throwable )
    {
        throw RuntimeException( "Failed to convert value [$value] of type [${ valueType.getName() }] to type [${ targetType.getName() }]",
                                t )
    }

    return assertNotNull( valueConverted,
                          "Unknown value [$value] of type [${ valueType.getName() }], can't be converted to [${ targetType.getName()}]" )
}


/**
 * Verifies none of objects specified is null.
 */
public inline fun verifyNotNull( vararg objects: Any? ): Unit = for ( o in objects ){ assertNotNull( o ) }


/**
 * Converts array to Map by passing each element to an operation and inserting a pair returned (key, value) into a Map.
 * Types:
 * T? - type of array elements, some elements can be null
 * K  - type of pair first element returned by an operation and used as map's key
 * V  - type of pair second element returned by an operation and used as map's value
 */
public inline fun <T, K, V> Array<T?>.convertToMap( operation: ( T ) -> Pair<K, V> ) : Map<K, V>
{
    val map : MutableMap<K, V> = hashMap()

    for ( o in this.filter{ it != null })
    {
        val pair = operation( o!! )
        map.put( pair.first , pair.second )
    }

    return map
}


/**
 * Converts collection to Map by passing each element to an operation and inserting a pair returned (key, value) into a Map.
 * Types:
 * T? - type of collection elements, some elements can be null
 * K  - type of pair first element returned by an operation and used as map's key
 * V  - type of pair second element returned by an operation and used as map's value
 */
public inline fun <T, K, V> Collection<T?>.convertToMap( operation: ( T ) -> Pair<K, V> ) : Map<K, V>
{
    val map : MutableMap<K, V> = hashMap()

    for ( o in this.filter{ it != null })
    {
        val pair = operation( o!! )
        map.put( pair.first , pair.second )
    }

    return map
}