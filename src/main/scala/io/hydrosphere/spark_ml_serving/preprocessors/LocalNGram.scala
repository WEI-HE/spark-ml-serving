package io.hydrosphere.spark_ml_serving.preprocessors

import io.hydrosphere.spark_ml_serving.TypedTransformerConverter
import io.hydrosphere.spark_ml_serving.common._
import org.apache.spark.ml.feature.NGram

class LocalNGram(override val sparkTransformer: NGram) extends LocalTransformer[NGram] {
  override def transform(localData: LocalData): LocalData = {
    localData.column(sparkTransformer.getInputCol) match {
      case Some(column) =>
        val method = classOf[NGram].getMethod("createTransformFunc")
        val f      = method.invoke(sparkTransformer).asInstanceOf[Seq[String] => Seq[String]]
        val data = column.data.map(_.asInstanceOf[Seq[String]]).map { row =>
          f.apply(row).toList
        }
        localData.withColumn(LocalDataColumn(sparkTransformer.getOutputCol, data))
      case None => localData
    }
  }
}

object LocalNGram extends SimpleModelLoader[NGram] with TypedTransformerConverter[NGram] {

  override def build(metadata: Metadata, data: LocalData): NGram = {
    new NGram(metadata.uid)
      .setN(metadata.paramMap("n").asInstanceOf[Number].intValue())
      .setInputCol(metadata.paramMap("inputCol").asInstanceOf[String])
      .setOutputCol(metadata.paramMap("outputCol").asInstanceOf[String])
  }

  override implicit def toLocal(transformer: NGram) =
    new LocalNGram(transformer)
}
