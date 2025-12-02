// src/components/Groups.tsx
import React, { useState, useEffect } from "react";
import apiClient from "../services/api-client";
import "../App.css";

interface Group {
  id: number;
  name: string;
  members: number;
  thumb: string;
}

const Groups: React.FC<{
  navigate: (to: "home" | "groups" | "receipts" | "profile" | "support") => void;
  openGroup: (groupId: number) => void;
}> = ({ navigate, openGroup }) => {
  const [groups, setGroups] = useState<Group[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchGroups = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get("/groups", {
        params: { offset: 0, limit: 100 },
      });
      const items = Array.isArray(res.data) ? res.data : [];

      // Fetch member count for each group
      const mapped: Group[] = await Promise.all(
        items.map(async (g: any) => {
          let memberCount = 0;
          try {
            const countRes = await apiClient.get(`/groups/${g.id}/users/nr`);
            memberCount = countRes.data || 0;
          } catch (err) {
            console.error(`Failed to fetch member count for group ${g.id}`, err);
          }

          return {
            id: g.id,
            name: g.name || "Unnamed Group",
            members: memberCount,
            thumb: (g.name?.[0] || "G").toUpperCase(),
          };
        })
      );

      setGroups(mapped);
    } catch (err) {
      console.error("Failed to fetch groups", err);
      setGroups([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGroups();
  }, []);

  const handleCreateGroup = async () => {
    const name = prompt("Group name");
    if (!name || !name.trim()) return;

    const description = prompt("Group description (optional)", "");

    try {
      const body: any = {
        name: name.trim(),
      };
      if (description && description.trim()) {
        body.description = description.trim();
      }

      await apiClient.post("/groups", body);
      await fetchGroups(); // Refresh the list
      alert("Group created successfully!");
    } catch (err: any) {
      console.error("Failed to create group", err);
      alert(err?.response?.data?.detail || "Failed to create group");
    }
  };

  const handleJoinGroup = async () => {
    const invitationCode = prompt("Enter invitation code (e.g., A2VC3B)");
    if (!invitationCode || !invitationCode.trim()) return;

    try {
      await apiClient.post(`/users/join-group/${invitationCode.trim()}`);
      await fetchGroups(); // Refresh the list
      alert("Successfully joined the group!");
    } catch (err: any) {
      console.error("Failed to join group", err);
      alert(err?.response?.data?.detail || "Failed to join group. Check the invitation code.");
    }
  };

  return (
    <>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginTop: 12,
        }}
      >
        <div className="bp-title">Groups</div>
        <button
          className="bp-add-btn"
          onClick={handleJoinGroup}
          style={{ fontSize: 14, padding: "8px 16px" }}
        >
          Join Group
        </button>
      </div>

      <div className="bp-section-title" style={{ marginTop: 12, fontSize: 15 }}>
        My Groups
      </div>

      <div style={{ marginTop: 10, display: "flex", flexDirection: "column", gap: 10 }}>
        {loading ? (
          <div style={{ textAlign: "center", padding: 20, color: "var(--muted-dark)" }}>
            Loading groups...
          </div>
        ) : groups.length === 0 ? (
          <div style={{ textAlign: "center", padding: 20, color: "var(--muted-dark)" }}>
            No groups yet. Create your first group!
          </div>
        ) : (
          groups.map((g) => (
            <div
              className="bp-group-card"
              key={g.id}
              onClick={() => openGroup(g.id)}
              style={{ cursor: "pointer" }}
              role="button"
              aria-label={`Open group ${g.name}`}
            >
              <div className="bp-group-thumb">{g.thumb}</div>
              <div style={{ display: "flex", flexDirection: "column" }}>
                <div style={{ fontWeight: 800 }}>{g.name}</div>
                <div style={{ color: "var(--muted-dark)", fontSize: 13, marginTop: 6 }}>
                  {g.members} members
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <button className="bp-create-group" onClick={handleCreateGroup}>
        + Create Group
      </button>
    </>
  );
};

export default Groups;
