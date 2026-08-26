from ..schemas import FlowerInfo
from ..flower_catalog import FLOWERS
from .common import FirestoreRepositoryBase


class FlowerRepository(FirestoreRepositoryBase):
    def __init__(self) -> None:
        self._flower_cache: dict[str, FlowerInfo] = {}

    def get_flower(self, flower_id: str | None) -> FlowerInfo | None:
        if not self.is_enabled or not flower_id:
            return None

        cached_flower = self._flower_cache.get(flower_id)
        if cached_flower is not None:
            return cached_flower

        doc = self._client().collection("flowers").document(flower_id).get()
        if not doc.exists:
            return None

        data = doc.to_dict() or {}
        data["id"] = data.get("id") or flower_id
        flower = FlowerInfo(**data)
        self._flower_cache[flower_id] = flower
        return flower

    def list_flowers(self) -> list[FlowerInfo]:
        flowers: list[FlowerInfo] = []
        for flower_id, fallback_flower in FLOWERS.items():
            flowers.append(self.get_flower(flower_id) or fallback_flower)
        return flowers
