package com.thenewmotion.ochp
package converters

import api.{RelatedResource, RelatedResourceTypes}
import eu.ochp._1.RelatedResourceType

object RelatedResourceConverter {

  def fromOchp(value: RelatedResourceType) =
    RelatedResource(value.getUri, RelatedResourceTypes.withName(value.getClazz))

  def toOchp(value: RelatedResource) =
    new RelatedResourceType {
      setUri(value.uri)
      setClazz(value.`class`.toString)
  }

}