import React from 'react';
import './ObjectSelector.css';

export default function ObjectSelector({ objects, selectedObject, onChange, disabled }) {
  return (
    <div className="object-selector">
      <label htmlFor="object-select" className="object-selector-label">
        Select Object
      </label>
      <select
        id="object-select"
        className="object-selector-dropdown"
        value={selectedObject}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
      >
        <option value="">-- Select an Object --</option>
        {objects.map((obj) => (
          <option key={obj} value={obj}>
            {obj}
          </option>
        ))}
      </select>
    </div>
  );
}
