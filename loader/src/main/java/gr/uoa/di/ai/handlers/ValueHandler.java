package gr.uoa.di.ai.handlers;

import gr.uoa.di.ai.constants.AllValueTypes;
import gr.uoa.di.ai.constants.LoaderGlobals;

public class ValueHandler {

    public static int assignId(AllValueTypes valueType){
        switch(valueType)
        {
            case URI:
                if(LoaderGlobals.uriNo==0)
                {
                    LoaderGlobals.uriNo = LoaderGlobals.minIds[AllValueTypes.URI.ordinal()] + 1;
                    return LoaderGlobals.uriNo;
                }
                else
                {
                    return ++LoaderGlobals.uriNo;
                }

            case URI_LONG:
                if(LoaderGlobals.uriLongNo==0)
                {
                    LoaderGlobals.uriLongNo = LoaderGlobals.minIds[AllValueTypes.URI_LONG.ordinal()];
                    return LoaderGlobals.uriLongNo;
                }
                else
                {
                    return ++LoaderGlobals.uriLongNo;
                }
            case BNODE:
                if(LoaderGlobals.bnodeNo==0)
                {
                    LoaderGlobals.bnodeNo = LoaderGlobals.minIds[AllValueTypes.BNODE.ordinal()] +1;
                    return LoaderGlobals.bnodeNo;
                }
                else
                {
                    return ++LoaderGlobals.bnodeNo;
                }
            case SIMPLE:
                if(LoaderGlobals.simpleNo==0)
                {
                    LoaderGlobals.simpleNo = LoaderGlobals.minIds[AllValueTypes.SIMPLE.ordinal()] +1;
                    return LoaderGlobals.simpleNo;
                }
                else
                {
                    return ++LoaderGlobals.simpleNo;
                }
            case SIMPLE_LONG:
                if(LoaderGlobals.simpleLongNo==0)
                {
                    LoaderGlobals.simpleLongNo = LoaderGlobals.minIds[AllValueTypes.SIMPLE_LONG.ordinal()] +1;
                    return LoaderGlobals.simpleLongNo;
                }
                else
                {
                    return ++LoaderGlobals.simpleLongNo;
                }
            case TYPED:
                if(LoaderGlobals.typedNo==0)
                {
                    LoaderGlobals.typedNo = LoaderGlobals.minIds[AllValueTypes.TYPED.ordinal()] +1;
                    return LoaderGlobals.typedNo;
                }
                else
                {
                    return ++LoaderGlobals.typedNo;
                }

            case TYPED_LONG:
                if(LoaderGlobals.typedLongNo==0)
                {
                    LoaderGlobals.typedLongNo = LoaderGlobals.minIds[AllValueTypes.TYPED_LONG.ordinal()] +1;
                    return LoaderGlobals.typedLongNo;
                }
                else
                {
                    return ++LoaderGlobals.typedLongNo;
                }
            case NUMERIC:
                if(LoaderGlobals.numericNo==0)
                {
                    LoaderGlobals.numericNo = LoaderGlobals.minIds[AllValueTypes.NUMERIC.ordinal()] +1;
                    return LoaderGlobals.numericNo;
                }
                else
                {
                    return ++LoaderGlobals.numericNo;
                }
            case DATETIME:
                if(LoaderGlobals.datetimeNo==0)
                {
                    LoaderGlobals.datetimeNo = LoaderGlobals.minIds[AllValueTypes.DATETIME.ordinal()] +1;
                    return LoaderGlobals.datetimeNo;
                }
                else
                {
                    return ++LoaderGlobals.datetimeNo;
                }
            case DATETIME_ZONED:
                if(LoaderGlobals.datetimeZonedNo==0)
                {
                    LoaderGlobals.datetimeZonedNo = LoaderGlobals.minIds[AllValueTypes.DATETIME_ZONED.ordinal()] +1;
                    return LoaderGlobals.datetimeZonedNo;
                }
                else
                {
                    return ++LoaderGlobals.datetimeZonedNo;
                }
            case LANG:
                if(LoaderGlobals.langNo==0)
                {
                    LoaderGlobals.langNo = LoaderGlobals.minIds[AllValueTypes.LANG.ordinal()] +1;
                    return LoaderGlobals.langNo;
                }
                else
                {
                    return ++LoaderGlobals.langNo;
                }
            case LANG_LONG:
                if(LoaderGlobals.langlongNo==0)
                {
                    LoaderGlobals.langlongNo = LoaderGlobals.minIds[AllValueTypes.LANG_LONG.ordinal()] +1;
                    return LoaderGlobals.langlongNo;
                }
                else
                {
                    return ++LoaderGlobals.langlongNo;
                }
            case XML:
                if(LoaderGlobals.xmlNo==0)
                {
                    LoaderGlobals.xmlNo = LoaderGlobals.minIds[AllValueTypes.XML.ordinal()] +1;
                    return LoaderGlobals.xmlNo;
                }
                else
                {
                    return ++LoaderGlobals.xmlNo;
                }
        }

        return -1;
    }

    public static int typeOfValue(String value, String valueType, String datatype, String language){
        if(valueType.equals("BNODE")){
            return AllValueTypes.BNODE.ordinal();
        }
        else if(valueType.equals("LITERAL")){

            if(datatype==null){
                if(language!=null){
                    if(isLong(value))
                        return AllValueTypes.LANG_LONG.ordinal();
                    else
                        return AllValueTypes.LANG.ordinal();
                }
                else{
                    if(isLong(value))
                        return AllValueTypes.SIMPLE_LONG.ordinal();
                    else
                        return AllValueTypes.SIMPLE.ordinal();
                }
            }
            else{
                if(isNumeric(datatype)) {
                    return  AllValueTypes.NUMERIC.ordinal();
                }
                else if(isCalendar(datatype)) {
                    if(isZoned(value))
                    {
                        return  AllValueTypes.DATETIME_ZONED.ordinal();
                    }
                    else
                    {
                        return  AllValueTypes.DATETIME.ordinal();
                    }
                }
                else if(isXML(datatype)){
                    return  AllValueTypes.XML.ordinal();
                }
                else if(isLong(value)) {
                    return  AllValueTypes.TYPED_LONG.ordinal();
                }
                else {
                    return  AllValueTypes.TYPED.ordinal();
                }
            }
        }
        else{
            if(isLong(value))
                return AllValueTypes.URI_LONG.ordinal();
            else
                return AllValueTypes.URI.ordinal();
        }
    }


    public static boolean isLong(String value){
        return value.length() > 255;
    }

    public static boolean isNumeric(String datatype) {
        return isDecimal(datatype) || isFloatingPoint(datatype);
    }


    public static boolean isDecimal(String datatype) {
        return datatype.equals("http://www.w3.org/2001/XMLSchema#decimal") || isInteger(datatype);
    }

    public static boolean isInteger(String datatype) {
        if(datatype.equals("http://www.w3.org/2001/XMLSchema#integer")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#long")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#int")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#short")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#byte")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#nonPositiveInteger")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#negativeInteger")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#positiveInteger")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#unsignedLong")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#unsignedInt")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#unsignedShort")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#unsignedByte")
        )
            return true;
        else return false;
    }

    public static boolean isFloatingPoint(String datatype) {
        if(datatype.equals("http://www.w3.org/2001/XMLSchema#float ")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#double")
        )
            return true;
        else return false;
    }

    public static boolean isCalendar(String datatype) {
        if(datatype.equals("http://www.w3.org/2001/XMLSchema#dateTime")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#date")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#time")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#gYearMonth")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#gMonthDay")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#gYear")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#gMonth")
                ||datatype.equals("http://www.w3.org/2001/XMLSchema#gDay")
        )
            return true;
        else return false;
    }

    public static boolean isXML(String datatype) {
        return datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
    }

    public static boolean isSpatial(String datatype) {
        return (datatype.equals("http://strdf.di.uoa.gr/ontology#WKT")
                ||datatype.equals("http://www.opengis.net/ont/geosparql#wktLiteral")
                ||datatype.equals("http://www.opengis.net/ont/geosparql#gmlLiteral")
        );
    }

    public static boolean isZoned(String literal)
    {
        int length = literal.length();
        if(length < 1)
            return false;

        if(literal.charAt(length - 1) == 'Z')
            return true;

        if (length < 6)
            return false;

        if(literal.charAt(length - 3) != ':')
            return false;

        char chr = literal.charAt(length - 6);

        return chr == '+' || chr == '-';
    }
}
