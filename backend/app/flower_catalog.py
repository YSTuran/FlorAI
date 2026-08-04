from .schemas import FlowerInfo


MODEL_LABEL_TO_FLOWER_ID = {
    "daisy": "papatya",
    "dandelion": "karahindiba",
    "roses": "gul",
    "sunflowers": "aycicegi",
    "tulips": "lale",
}


FLOWERS = {
    "papatya": FlowerInfo(
        id="papatya",
        commonName="Papatya",
        scientificName="Bellis perennis",
        height="10-20 cm",
        habitats=["Cayirlar", "yol kenarlari", "bahceler", "acik ve gunesli alanlar"],
        bloomMonths=["Mart", "Nisan", "Mayis", "Haziran", "Temmuz", "Agustos", "Eylul", "Ekim"],
        details=(
            "Papatya, iliman iklimlerde yaygin gorulen, beyaz tac yapraklari ve sari "
            "orta kismi ile kolay taninan bir cicektir. Bahce ve dogal alanlarda sik "
            "karsilasilir."
        ),
        extraFacts=[
            "Gunesli veya yari golgeli alanlarda iyi gelisir.",
            "Benzer gorunumlu farkli papatya turleri bulunabilir.",
        ],
    ),
    "karahindiba": FlowerInfo(
        id="karahindiba",
        commonName="Karahindiba",
        scientificName="Taraxacum officinale",
        height="5-40 cm",
        habitats=["Cayirlar", "tarla kenarlari", "yol kenarlari", "park ve cim alanlari"],
        bloomMonths=["Mart", "Nisan", "Mayis", "Haziran", "Temmuz", "Agustos", "Eylul", "Ekim", "Kasim"],
        details=(
            "Karahindiba parlak sari cicekleri ve daha sonra olusan tuylu tohum basi ile "
            "bilinir. Dayanikli yapisi sayesinde sehir icinde ve dogal alanlarda yaygin "
            "olarak gorulebilir."
        ),
        extraFacts=[
            "Tohumlari ruzgarla kolayca yayilir.",
            "Genellikle dusuk bakimli ve direncli bir bitkidir.",
        ],
    ),
    "gul": FlowerInfo(
        id="gul",
        commonName="Gul",
        scientificName="Rosa spp.",
        height="30-200 cm",
        habitats=["Bahceler", "parklar", "sus bitkisi alanlari", "iliman bolgeler"],
        bloomMonths=["Mayis", "Haziran", "Temmuz", "Agustos", "Eylul", "Ekim"],
        details=(
            "Gul, cok sayida tur ve kulture sahip, kokusu ve gosterisli cicekleriyle "
            "taninan bir sus bitkisidir. Renk, boy ve diken yapisi ture gore degisiklik "
            "gosterebilir."
        ),
        extraFacts=[
            "Duzenli budama ciceklenmeyi destekleyebilir.",
            "Tam gunes alan konumlarda daha verimli gelisir.",
        ],
    ),
    "aycicegi": FlowerInfo(
        id="aycicegi",
        commonName="Aycicegi",
        scientificName="Helianthus annuus",
        height="100-300 cm",
        habitats=["Tarim alanlari", "gunesli bahceler", "acik araziler"],
        bloomMonths=["Temmuz", "Agustos", "Eylul"],
        details=(
            "Aycicegi buyuk sari cicek tablasi ve uzun govdesiyle bilinen tek yillik "
            "bir bitkidir. Gunesli alanlari sever ve tarimsal uretimde de onemli bir "
            "yere sahiptir."
        ),
        extraFacts=[
            "Genclik doneminde gunes yonelimli hareket gosterebilir.",
            "Tohumlari gida ve yag uretiminde kullanilir.",
        ],
    ),
    "lale": FlowerInfo(
        id="lale",
        commonName="Lale",
        scientificName="Tulipa spp.",
        height="10-70 cm",
        habitats=["Bahceler", "parklar", "step alanlari", "kayalik yamaclar"],
        bloomMonths=["Mart", "Nisan", "Mayis"],
        details=(
            "Lale, soganli yapisi ve canli renkli cicekleriyle taninan ilkbahar "
            "bitkisidir. Sus bitkisi olarak yaygin yetistirilir ve farkli renklerde "
            "cok sayida kulture sahiptir."
        ),
        extraFacts=[
            "Dikim genellikle sonbaharda yapilir.",
            "Ciceklenme suresi iklim ve ture gore degisebilir.",
        ],
    ),
}


def get_flower_id_for_model_label(model_label: str) -> str | None:
    return MODEL_LABEL_TO_FLOWER_ID.get(model_label)


def get_flower_by_model_label(model_label: str) -> FlowerInfo | None:
    flower_id = get_flower_id_for_model_label(model_label)
    if flower_id is None:
        return None
    return FLOWERS.get(flower_id)
