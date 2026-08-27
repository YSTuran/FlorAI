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
        habitats=["Çayırlar", "yol kenarları", "bahçeler", "açık ve güneşli alanlar"],
        bloomMonths=["Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim"],
        details=(
            "Papatya, ılıman iklimlerde yaygın görülen, beyaz taç yaprakları ve sarı "
            "orta kısmı ile kolay tanınan bir çiçektir. Bahçe ve doğal alanlarda sık "
            "karşılaşılır."
        ),
        extraFacts=[
            "Güneşli veya yarı gölgeli alanlarda iyi gelişir.",
            "Benzer görünümlü farklı papatya türleri bulunabilir.",
        ],
    ),
    "karahindiba": FlowerInfo(
        id="karahindiba",
        commonName="Karahindiba",
        scientificName="Taraxacum officinale",
        height="5-40 cm",
        habitats=["Çayırlar", "tarla kenarları", "yol kenarları", "park ve çim alanları"],
        bloomMonths=["Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım"],
        details=(
            "Karahindiba parlak sarı çiçekleri ve daha sonra oluşan tüylü tohum başı ile "
            "bilinir. Dayanıklı yapısı sayesinde şehir içinde ve doğal alanlarda yaygın "
            "olarak görülebilir."
        ),
        extraFacts=[
            "Tohumları rüzgarla kolayca yayılır.",
            "Genellikle düşük bakımlı ve dirençli bir bitkidir.",
        ],
    ),
    "gul": FlowerInfo(
        id="gul",
        commonName="Gül",
        scientificName="Rosa spp.",
        height="30-200 cm",
        habitats=["Bahçeler", "parklar", "süs bitkisi alanları", "ılıman bölgeler"],
        bloomMonths=["Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim"],
        details=(
            "Gül, çok sayıda tür ve kültüre sahip, kokusu ve gösterişli çiçekleriyle "
            "tanınan bir süs bitkisidir. Renk, boy ve diken yapısı türe göre değişiklik "
            "gösterebilir."
        ),
        extraFacts=[
            "Düzenli budama çiçeklenmeyi destekleyebilir.",
            "Tam güneş alan konumlarda daha verimli gelişir.",
        ],
    ),
    "aycicegi": FlowerInfo(
        id="aycicegi",
        commonName="Ayçiçeği",
        scientificName="Helianthus annuus",
        height="100-300 cm",
        habitats=["Tarım alanları", "güneşli bahçeler", "açık araziler"],
        bloomMonths=["Temmuz", "Ağustos", "Eylül"],
        details=(
            "Ayçiçeği büyük sarı çiçek tablası ve uzun gövdesiyle bilinen tek yıllık "
            "bir bitkidir. Güneşli alanları sever ve tarımsal üretimde de önemli bir "
            "yere sahiptir."
        ),
        extraFacts=[
            "Gençlik döneminde güneş yönelimli hareket gösterebilir.",
            "Tohumları gıda ve yağ üretiminde kullanılır.",
        ],
    ),
    "lale": FlowerInfo(
        id="lale",
        commonName="Lale",
        scientificName="Tulipa spp.",
        height="10-70 cm",
        habitats=["Bahçeler", "parklar", "step alanları", "kayalık yamaçlar"],
        bloomMonths=["Mart", "Nisan", "Mayıs"],
        details=(
            "Lale, soğanlı yapısı ve canlı renkli çiçekleriyle tanınan ilkbahar "
            "bitkisidir. Süs bitkisi olarak yaygın yetiştirilir ve farklı renklerde "
            "çok sayıda kültüre sahiptir."
        ),
        extraFacts=[
            "Dikim genellikle sonbaharda yapılır.",
            "Çiçeklenme süresi iklim ve türe göre değişebilir.",
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
